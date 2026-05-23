package com.titanclone.titan_clone.gms

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Manages per-clone Google account isolation.
 *
 * Each clone must have its own independent Google account session —
 * no sharing of OAuth tokens, account cookies, or session data
 * between clones. This is critical for Play Store cloning where
 * each clone signs into a different Gmail account.
 *
 * Isolation strategy:
 *   - Separate AccountManager data directory per clone
 *   - Independent OAuth2 token storage
 *   - Per-clone cookie jar for Google auth endpoints
 *   - Redirect all AccountManager service calls to clone-specific storage
 */
class AccountIsolationManager(private val context: Context) {

    companion object {
        private const val TAG = "AccountIsolation"
        private const val ACCOUNTS_DIR = "accounts"
        private const val TOKENS_DIR = "tokens"
    }

    data class CloneAccount(
        val cloneId: String,
        val email: String,
        val accountType: String,
        val authToken: String?,
        val addedAt: Long
    )

    private val cloneAccounts = mutableMapOf<String, MutableList<CloneAccount>>()

    /**
     * Set up isolated account storage for a new clone.
     */
    fun initializeForClone(cloneId: String) {
        try {
            val accountDir = getAccountDir(cloneId)
            accountDir.mkdirs()
            File(accountDir, TOKENS_DIR).mkdirs()
            cloneAccounts.getOrPut(cloneId) { mutableListOf() }
            Log.d(TAG, "Initialized account isolation for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize account isolation for $cloneId", e)
        }
    }

    /**
     * Register a Google account sign-in for a specific clone.
     */
    fun addAccount(cloneId: String, email: String, authToken: String? = null) {
        val account = CloneAccount(
            cloneId = cloneId,
            email = email,
            accountType = "com.google",
            authToken = authToken,
            addedAt = System.currentTimeMillis()
        )
        cloneAccounts.getOrPut(cloneId) { mutableListOf() }.add(account)
        persistAccount(cloneId, account)
        Log.d(TAG, "Added account $email to clone $cloneId")
    }

    /**
     * Store an OAuth token for a clone's account.
     */
    fun storeAuthToken(cloneId: String, email: String, tokenType: String, token: String) {
        try {
            val tokenFile = File(getAccountDir(cloneId), "$TOKENS_DIR/${email}_$tokenType.token")
            tokenFile.writeText(token)
            Log.d(TAG, "Stored $tokenType token for $email in clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store token for $email in clone $cloneId", e)
        }
    }

    /**
     * Retrieve a stored OAuth token for a clone's account.
     */
    fun getAuthToken(cloneId: String, email: String, tokenType: String): String? {
        return try {
            val tokenFile = File(getAccountDir(cloneId), "$TOKENS_DIR/${email}_$tokenType.token")
            if (tokenFile.exists()) tokenFile.readText() else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get token for $email in clone $cloneId", e)
            null
        }
    }

    /**
     * Invalidate all tokens for a specific account in a clone.
     */
    fun invalidateTokens(cloneId: String, email: String) {
        try {
            val tokensDir = File(getAccountDir(cloneId), TOKENS_DIR)
            tokensDir.listFiles()?.filter { it.name.startsWith(email) }?.forEach { it.delete() }
            Log.d(TAG, "Invalidated tokens for $email in clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to invalidate tokens for $email in clone $cloneId", e)
        }
    }

    /**
     * Remove an account from a clone.
     */
    fun removeAccount(cloneId: String, email: String) {
        cloneAccounts[cloneId]?.removeAll { it.email == email }
        invalidateTokens(cloneId, email)
        try {
            val accountFile = File(getAccountDir(cloneId), "${email}.json")
            accountFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove account $email from clone $cloneId", e)
        }
        Log.d(TAG, "Removed account $email from clone $cloneId")
    }

    /**
     * Get all accounts registered in a specific clone.
     */
    fun getAccounts(cloneId: String): List<CloneAccount> {
        return cloneAccounts[cloneId]?.toList() ?: emptyList()
    }

    /**
     * Get the primary (first) account for a clone.
     */
    fun getPrimaryAccount(cloneId: String): CloneAccount? {
        return cloneAccounts[cloneId]?.firstOrNull()
    }

    /**
     * Clean up all account data for a clone (on deletion).
     */
    fun cleanupClone(cloneId: String) {
        try {
            getAccountDir(cloneId).deleteRecursively()
            cloneAccounts.remove(cloneId)
            Log.d(TAG, "Cleaned up account data for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup accounts for clone $cloneId", e)
        }
    }

    /**
     * Get the isolated account storage directory for a clone.
     */
    private fun getAccountDir(cloneId: String): File {
        return File(context.filesDir, "clones/$cloneId/$ACCOUNTS_DIR")
    }

    private fun persistAccount(cloneId: String, account: CloneAccount) {
        try {
            val accountFile = File(getAccountDir(cloneId), "${account.email}.json")
            val json = buildString {
                append("{")
                append("\"email\":\"${account.email}\",")
                append("\"accountType\":\"${account.accountType}\",")
                append("\"addedAt\":${account.addedAt}")
                append("}")
            }
            accountFile.writeText(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist account ${account.email}", e)
        }
    }
}
