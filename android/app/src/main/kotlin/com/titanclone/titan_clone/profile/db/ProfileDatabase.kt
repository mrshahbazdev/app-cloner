package com.titanclone.titan_clone.profile.db

import android.content.Context
import android.util.Log

/**
 * Profile database manager.
 *
 * TODO: Convert to Room @Database when Room is integrated.
 * Current implementation uses SharedPreferences via VirtualProfileManager.
 *
 * Target Room schema:
 *   @Database(entities = [VirtualProfileEntity::class], version = 1)
 *   abstract class ProfileDatabase : RoomDatabase() {
 *       abstract fun profileDao(): VirtualProfileDao
 *   }
 */
class ProfileDatabase private constructor(context: Context) {

    companion object {
        private const val TAG = "ProfileDatabase"
        private const val DB_NAME = "profiles.db"

        @Volatile
        private var instance: ProfileDatabase? = null

        fun getInstance(context: Context): ProfileDatabase {
            return instance ?: synchronized(this) {
                instance ?: ProfileDatabase(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    init {
        Log.d(TAG, "Profile database initialized (SharedPreferences mode)")
    }
}
