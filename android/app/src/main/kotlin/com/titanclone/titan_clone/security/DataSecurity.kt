package com.titanclone.titan_clone.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Data security for clone storage and profile databases.
 *
 * Security measures:
 *   1. Profile encryption — AES-256-CBC with device-specific key
 *   2. Clone data encryption — optional filesystem-level encryption
 *   3. Secure IPC — Binder caller identity verification
 *   4. No data leakage — android:allowBackup="false" for virtual data
 *   5. Secure deletion — overwrite before delete for sensitive files
 */
class DataSecurity(private val context: Context) {

    companion object {
        private const val TAG = "DataSecurity"
        private const val KEY_ALGORITHM = "AES"
        private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding"
        private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val KEY_LENGTH = 256
        private const val ITERATION_COUNT = 65536
        private const val IV_LENGTH = 16
        private const val SALT_LENGTH = 32
    }

    private var cachedKey: SecretKeySpec? = null

    /**
     * Derive an encryption key from the device-specific hardware ID.
     */
    private fun deriveKey(salt: ByteArray): SecretKeySpec {
        cachedKey?.let { return it }

        val deviceId = getDeviceSpecificId()
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val spec = PBEKeySpec(deviceId.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val secret = factory.generateSecret(spec)
        val key = SecretKeySpec(secret.encoded, KEY_ALGORITHM)
        cachedKey = key
        return key
    }

    /**
     * Encrypt data using AES-256-CBC with a device-specific key.
     */
    fun encrypt(data: ByteArray): EncryptedData {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(salt)

        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(data)

        return EncryptedData(
            ciphertext = encrypted,
            iv = iv,
            salt = salt
        )
    }

    /**
     * Decrypt data using AES-256-CBC.
     */
    fun decrypt(encryptedData: EncryptedData): ByteArray {
        val key = deriveKey(encryptedData.salt)
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(encryptedData.iv))
        return cipher.doFinal(encryptedData.ciphertext)
    }

    /**
     * Encrypt a file in place.
     */
    fun encryptFile(file: File): Boolean {
        return try {
            val plaintext = file.readBytes()
            val encrypted = encrypt(plaintext)
            val outputFile = File(file.parent, "${file.name}.enc")
            outputFile.outputStream().use { out ->
                out.write(encrypted.salt)
                out.write(encrypted.iv)
                out.write(encrypted.ciphertext)
            }
            secureDelete(file)
            outputFile.renameTo(file)
            Log.d(TAG, "Encrypted file: ${file.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt file: ${file.name}", e)
            false
        }
    }

    /**
     * Decrypt a file in place.
     */
    fun decryptFile(file: File): Boolean {
        return try {
            val raw = file.readBytes()
            if (raw.size < SALT_LENGTH + IV_LENGTH) return false

            val salt = raw.sliceArray(0 until SALT_LENGTH)
            val iv = raw.sliceArray(SALT_LENGTH until SALT_LENGTH + IV_LENGTH)
            val ciphertext = raw.sliceArray(SALT_LENGTH + IV_LENGTH until raw.size)

            val decrypted = decrypt(EncryptedData(ciphertext, iv, salt))
            file.writeBytes(decrypted)
            Log.d(TAG, "Decrypted file: ${file.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt file: ${file.name}", e)
            false
        }
    }

    /**
     * Encrypt an entire clone's data directory.
     */
    fun encryptCloneData(cloneId: String): Boolean {
        val cloneDir = File(context.filesDir, "clones/$cloneId")
        if (!cloneDir.isDirectory) return false

        var success = true
        cloneDir.walkTopDown()
            .filter { it.isFile && !it.name.endsWith(".enc") }
            .filter { it.extension in listOf("json", "db", "token", "xml") }
            .forEach { file ->
                if (!encryptFile(file)) {
                    success = false
                }
            }
        return success
    }

    /**
     * Securely delete a file by overwriting with random data before deletion.
     */
    fun secureDelete(file: File): Boolean {
        return try {
            if (!file.exists()) return true
            val length = file.length()
            val random = SecureRandom()
            file.outputStream().use { out ->
                val buffer = ByteArray(4096)
                var remaining = length
                while (remaining > 0) {
                    random.nextBytes(buffer)
                    val toWrite = minOf(remaining, buffer.size.toLong()).toInt()
                    out.write(buffer, 0, toWrite)
                    remaining -= toWrite
                }
            }
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Secure delete failed: ${file.name}", e)
            file.delete()
        }
    }

    /**
     * Securely delete an entire clone's data directory.
     */
    fun secureDeleteCloneData(cloneId: String): Boolean {
        val cloneDir = File(context.filesDir, "clones/$cloneId")
        if (!cloneDir.isDirectory) return true

        var success = true
        cloneDir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                if (!secureDelete(file)) success = false
            }
        cloneDir.deleteRecursively()
        return success
    }

    /**
     * Verify Binder caller identity for IPC security.
     */
    fun verifyCallerIdentity(callingUid: Int): Boolean {
        val myUid = android.os.Process.myUid()
        return callingUid == myUid
    }

    /**
     * Get a device-specific identifier for key derivation.
     */
    @Suppress("HardwareIds")
    private fun getDeviceSpecificId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "default"

        return "$androidId-${Build.BOARD}-${Build.BOOTLOADER}-${context.packageName}"
    }

    data class EncryptedData(
        val ciphertext: ByteArray,
        val iv: ByteArray,
        val salt: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EncryptedData) return false
            return ciphertext.contentEquals(other.ciphertext)
                    && iv.contentEquals(other.iv)
                    && salt.contentEquals(other.salt)
        }

        override fun hashCode(): Int {
            var result = ciphertext.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            result = 31 * result + salt.contentHashCode()
            return result
        }
    }
}
