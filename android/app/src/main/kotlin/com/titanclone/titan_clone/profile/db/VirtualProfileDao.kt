package com.titanclone.titan_clone.profile.db

/**
 * Data Access Object for virtual profiles.
 *
 * TODO: Convert to Room @Dao with @Query annotations.
 * Currently backed by JSON/SharedPreferences in ProfileDatabase.
 */
interface VirtualProfileDao {
    fun insert(profile: VirtualProfileEntity)
    fun update(profile: VirtualProfileEntity)
    fun delete(cloneId: String)
    fun getById(cloneId: String): VirtualProfileEntity?
    fun getAll(): List<VirtualProfileEntity>
    fun getByPackageAndUser(packageName: String, userId: Int): VirtualProfileEntity?

    /** Check if any existing profile uses the given androidId */
    fun existsWithAndroidId(androidId: String): Boolean

    /** Check if any existing profile uses the given IMEI */
    fun existsWithImei(imei: String): Boolean

    /** Check if any existing profile uses the given MAC address */
    fun existsWithMac(mac: String): Boolean

    /** Check if any existing profile uses the given GSF ID */
    fun existsWithGsfId(gsfId: String): Boolean

    /** Get all profiles for a given package (across all users) */
    fun getByPackage(packageName: String): List<VirtualProfileEntity>
}
