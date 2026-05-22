package com.titanclone.titan_clone.profile

import android.content.Context
import android.util.Log
import com.titanclone.titan_clone.profile.db.ProfileDatabase
import com.titanclone.titan_clone.profile.db.VirtualProfileEntity

/**
 * High-level profile management — orchestrates ProfileGenerator
 * and ProfileDatabase to create, store, and retrieve virtual profiles.
 */
class VirtualProfileManager(context: Context) {

    companion object {
        private const val TAG = "VirtualProfileMgr"
    }

    private val db = ProfileDatabase.getInstance(context)
    private val generator = ProfileGenerator(db.dao)

    /**
     * Get or create a profile for a clone. If a profile already exists,
     * return it. Otherwise generate a new one and persist it.
     */
    fun getOrCreateProfile(packageName: String, userId: Int): VirtualProfileEntity {
        val existing = db.dao.getByPackageAndUser(packageName, userId)
        if (existing != null) return existing

        val profile = generator.generateFullProfile(packageName, userId)
        val errors = generator.validateProfile(profile)
        if (errors.isNotEmpty()) {
            Log.w(TAG, "Generated profile has validation issues: $errors")
        }

        db.dao.insert(profile)
        Log.i(TAG, "Created profile for ${profile.cloneId} (preset=${profile.profilePreset})")
        return profile
    }

    fun getProfile(cloneId: String): VirtualProfileEntity? {
        return db.dao.getById(cloneId)
    }

    fun updateProfile(profile: VirtualProfileEntity) {
        db.dao.update(profile)
    }

    fun deleteProfile(cloneId: String) {
        db.dao.delete(cloneId)
    }

    fun getAllProfiles(): List<VirtualProfileEntity> {
        return db.dao.getAll()
    }

    /**
     * Set proxy config for per-clone IP isolation.
     */
    fun setProxyConfig(
        cloneId: String, host: String, port: Int,
        user: String? = null, pass: String? = null, dns: String? = null
    ) {
        val profile = db.dao.getById(cloneId) ?: return
        val updated = profile.copy(
            proxyHost = host,
            proxyPort = port,
            proxyUser = user,
            proxyPass = pass,
            dnsServer = dns
        )
        db.dao.update(updated)
    }

    /**
     * Get the 126+ system properties for native injection.
     */
    fun getSystemProperties(cloneId: String): Map<String, String> {
        val profile = db.dao.getById(cloneId) ?: return emptyMap()
        return generator.getSystemProperties(profile)
    }
}
