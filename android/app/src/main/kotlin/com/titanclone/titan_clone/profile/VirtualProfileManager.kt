package com.titanclone.titan_clone.profile

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class VirtualProfileManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "titan_clone_profiles"
    }

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveProfile(packageName: String, userId: Int, profile: Map<String, Any>) {
        val key = "${packageName}_clone_${userId}"
        val json = JSONObject(profile).toString()
        prefs.edit().putString(key, json).apply()
    }

    fun getProfile(cloneId: String): Map<String, Any>? {
        val json = prefs.getString(cloneId, null) ?: return null
        return try {
            val jsonObj = JSONObject(json)
            jsonObj.keys().asSequence().associateWith { key ->
                jsonObj.get(key) as Any
            }
        } catch (_: Exception) {
            null
        }
    }

    fun updateProfile(cloneId: String, profileData: Map<String, Any>) {
        val existing = getProfile(cloneId)?.toMutableMap() ?: mutableMapOf()
        existing.putAll(profileData)
        val json = JSONObject(existing).toString()
        prefs.edit().putString(cloneId, json).apply()
    }

    fun deleteProfile(cloneId: String) {
        prefs.edit().remove(cloneId).apply()
    }

    fun getAllProfiles(): Map<String, Map<String, Any>> {
        return prefs.all.mapNotNull { (key, value) ->
            if (value is String) {
                try {
                    val jsonObj = JSONObject(value)
                    key to jsonObj.keys().asSequence().associateWith { k ->
                        jsonObj.get(k) as Any
                    }
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
        }.toMap()
    }
}
