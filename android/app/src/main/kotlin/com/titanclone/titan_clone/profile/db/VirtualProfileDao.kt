package com.titanclone.titan_clone.profile.db

/**
 * Data Access Object interface for virtual profiles.
 *
 * TODO: Convert to Room @Dao when Room database is integrated.
 * Currently profile persistence is handled by VirtualProfileManager
 * using SharedPreferences as a temporary solution.
 */
interface VirtualProfileDao {
    fun insert(profile: VirtualProfileEntity)
    fun update(profile: VirtualProfileEntity)
    fun delete(cloneId: String)
    fun getById(cloneId: String): VirtualProfileEntity?
    fun getAll(): List<VirtualProfileEntity>
    fun getByPackageAndUser(packageName: String, userId: Int): VirtualProfileEntity?
}
