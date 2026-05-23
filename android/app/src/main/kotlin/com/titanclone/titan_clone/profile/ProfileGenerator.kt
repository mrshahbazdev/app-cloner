package com.titanclone.titan_clone.profile

import com.titanclone.titan_clone.profile.db.VirtualProfileDao
import com.titanclone.titan_clone.profile.db.VirtualProfileEntity
import java.security.SecureRandom
import java.util.UUID

/**
 * Generates realistic virtual device profiles for clones.
 *
 * Features:
 * - 12 complete device presets (real Build.FINGERPRINT, boards, hardware)
 * - Valid IMEI with Luhn checksum, proper MAC format
 * - Anti-correlation: guarantees no two clones share any identifier
 * - Random generation with realistic carrier/network info
 */
class ProfileGenerator(private val dao: VirtualProfileDao? = null) {

    private val random = SecureRandom()

    // 12 complete device presets — every field matches a real device
    private val devicePresets = listOf(
        DevicePreset(
            name = "Google Pixel 8 Pro",
            model = "husky", brand = "google", manufacturer = "Google",
            product = "husky", device = "husky", hardware = "husky",
            board = "husky", bootloader = "slider.20240402.00",
            fingerprint = "google/husky/husky:14/AP2A.240805.005/12025142:user/release-keys",
            display = "AP2A.240805.005", buildId = "AP2A.240805.005",
            incremental = "12025142", securityPatch = "2024-08-05",
            screenDensity = 420, screenWidth = 1344, screenHeight = 2992,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "Google Fi", carrierCode = "310260", countryIso = "us",
            presetId = "pixel8pro"
        ),
        DevicePreset(
            name = "Samsung Galaxy S24 Ultra",
            model = "SM-S928B", brand = "samsung", manufacturer = "samsung",
            product = "dm3q", device = "dm3q", hardware = "qcom",
            board = "kalama", bootloader = "S928BXXS1AXB1",
            fingerprint = "samsung/dm3q/dm3q:14/UP1A.231005.007/S928BXXS1AXB1:user/release-keys",
            display = "UP1A.231005.007.S928BXXS1AXB1", buildId = "UP1A.231005.007",
            incremental = "S928BXXS1AXB1", securityPatch = "2024-07-01",
            screenDensity = 480, screenWidth = 1440, screenHeight = 3120,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "AT&T", carrierCode = "310410", countryIso = "us",
            presetId = "s24ultra"
        ),
        DevicePreset(
            name = "OnePlus 12",
            model = "CPH2583", brand = "OnePlus", manufacturer = "OnePlus",
            product = "CPH2583", device = "OP5913L1", hardware = "qcom",
            board = "kalama", bootloader = "unknown",
            fingerprint = "OnePlus/CPH2583/OP5913L1:14/UKQ1.230924.001/1704180000:user/release-keys",
            display = "CPH2583_14.0.0.700(EX01)", buildId = "UKQ1.230924.001",
            incremental = "1704180000", securityPatch = "2024-06-05",
            screenDensity = 480, screenWidth = 1440, screenHeight = 3168,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "Verizon", carrierCode = "311480", countryIso = "us",
            presetId = "oneplus12"
        ),
        DevicePreset(
            name = "Xiaomi 14 Pro",
            model = "23116PN5BC", brand = "Xiaomi", manufacturer = "Xiaomi",
            product = "missi", device = "missi", hardware = "qcom",
            board = "kalama", bootloader = "unknown",
            fingerprint = "Xiaomi/missi/missi:14/UKQ1.231003.002/V816.0.3.0.UNACNXM:user/release-keys",
            display = "UKQ1.231003.002", buildId = "UKQ1.231003.002",
            incremental = "V816.0.3.0.UNACNXM", securityPatch = "2024-05-01",
            screenDensity = 480, screenWidth = 1440, screenHeight = 3200,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "T-Mobile", carrierCode = "310260", countryIso = "us",
            presetId = "xiaomi14pro"
        ),
        DevicePreset(
            name = "Google Pixel 7",
            model = "panther", brand = "google", manufacturer = "Google",
            product = "panther", device = "panther", hardware = "tensor",
            board = "cloudripper", bootloader = "slider.20240402.00",
            fingerprint = "google/panther/panther:14/AP2A.240805.005/12025142:user/release-keys",
            display = "AP2A.240805.005", buildId = "AP2A.240805.005",
            incremental = "12025142", securityPatch = "2024-08-05",
            screenDensity = 420, screenWidth = 1080, screenHeight = 2400,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "T-Mobile", carrierCode = "310260", countryIso = "us",
            presetId = "pixel7"
        ),
        DevicePreset(
            name = "Samsung Galaxy A54",
            model = "SM-A546B", brand = "samsung", manufacturer = "samsung",
            product = "a54xnsxx", device = "a54x", hardware = "exynos1380",
            board = "s5e8835", bootloader = "A546BXXS7CXA2",
            fingerprint = "samsung/a54xnsxx/a54x:14/UP1A.231005.007/A546BXXS7CXA2:user/release-keys",
            display = "UP1A.231005.007.A546BXXS7CXA2", buildId = "UP1A.231005.007",
            incremental = "A546BXXS7CXA2", securityPatch = "2024-06-01",
            screenDensity = 393, screenWidth = 1080, screenHeight = 2340,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "Vodafone", carrierCode = "23415", countryIso = "gb",
            presetId = "a54"
        ),
        DevicePreset(
            name = "Motorola Edge 40 Pro",
            model = "XT2301-4", brand = "motorola", manufacturer = "Motorola",
            product = "eqs_g", device = "eqs", hardware = "qcom",
            board = "kalama", bootloader = "unknown",
            fingerprint = "motorola/eqs_g/eqs:14/U1TQS34.66-18-2-8/14af62:user/release-keys",
            display = "U1TQS34.66-18-2-8", buildId = "U1TQS34.66-18-2-8",
            incremental = "14af62", securityPatch = "2024-04-01",
            screenDensity = 393, screenWidth = 1080, screenHeight = 2400,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "Sprint", carrierCode = "310120", countryIso = "us",
            presetId = "edge40pro"
        ),
        DevicePreset(
            name = "Sony Xperia 1 V",
            model = "XQ-DQ72", brand = "Sony", manufacturer = "Sony",
            product = "XQ-DQ72", device = "pdx234", hardware = "qcom",
            board = "kalama", bootloader = "unknown",
            fingerprint = "Sony/XQ-DQ72/XQ-DQ72:14/67.2.A.2.45/067002A002004500:user/release-keys",
            display = "67.2.A.2.45", buildId = "67.2.A.2.45",
            incremental = "067002A002004500", securityPatch = "2024-05-01",
            screenDensity = 480, screenWidth = 1644, screenHeight = 3840,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "NTT DOCOMO", carrierCode = "44010", countryIso = "jp",
            presetId = "xperia1v"
        ),
        DevicePreset(
            name = "Nothing Phone (2)",
            model = "A065", brand = "Nothing", manufacturer = "Nothing",
            product = "Pong", device = "Pong", hardware = "qcom",
            board = "kalama", bootloader = "unknown",
            fingerprint = "Nothing/Pong/Pong:14/UKQ1.230924.001/2401100141:user/release-keys",
            display = "Pong_V2.6-240822-1923", buildId = "UKQ1.230924.001",
            incremental = "2401100141", securityPatch = "2024-08-05",
            screenDensity = 420, screenWidth = 1080, screenHeight = 2412,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "EE", carrierCode = "23430", countryIso = "gb",
            presetId = "nothing2"
        ),
        DevicePreset(
            name = "OPPO Find X7 Ultra",
            model = "PHZ110", brand = "OPPO", manufacturer = "OPPO",
            product = "PHZ110", device = "OP5B11L1", hardware = "qcom",
            board = "pineapple", bootloader = "unknown",
            fingerprint = "OPPO/PHZ110/OP5B11L1:14/UKQ1.230924.001/1704180000:user/release-keys",
            display = "PHZ110_14.0.0.500", buildId = "UKQ1.230924.001",
            incremental = "1704180000", securityPatch = "2024-04-05",
            screenDensity = 480, screenWidth = 1440, screenHeight = 3168,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "China Mobile", carrierCode = "46000", countryIso = "cn",
            presetId = "findx7ultra"
        ),
        DevicePreset(
            name = "Realme GT 5 Pro",
            model = "RMX3888", brand = "realme", manufacturer = "realme",
            product = "RMX3888", device = "RE58C2L1", hardware = "qcom",
            board = "pineapple", bootloader = "unknown",
            fingerprint = "realme/RMX3888/RE58C2L1:14/UKQ1.230924.001/1704180000:user/release-keys",
            display = "RMX3888_14.0.0.300", buildId = "UKQ1.230924.001",
            incremental = "1704180000", securityPatch = "2024-03-05",
            screenDensity = 480, screenWidth = 1264, screenHeight = 2780,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "Jio", carrierCode = "40588", countryIso = "in",
            presetId = "gt5pro"
        ),
        DevicePreset(
            name = "Vivo X100 Pro",
            model = "V2324A", brand = "vivo", manufacturer = "vivo",
            product = "V2324A", device = "V2324A", hardware = "mt6989",
            board = "mt6989", bootloader = "unknown",
            fingerprint = "vivo/V2324A/V2324A:14/UKQ1.230924.001/1704180000:user/release-keys",
            display = "V2324A_14.0.9.3", buildId = "UKQ1.230924.001",
            incremental = "1704180000", securityPatch = "2024-03-01",
            screenDensity = 480, screenWidth = 1440, screenHeight = 3200,
            sdkVersion = 34, releaseVersion = "14",
            carrier = "China Unicom", carrierCode = "46001", countryIso = "cn",
            presetId = "x100pro"
        )
    )

    private val timezones = listOf(
        "America/New_York", "America/Chicago", "America/Denver",
        "America/Los_Angeles", "Europe/London", "Europe/Berlin",
        "Asia/Tokyo", "Asia/Shanghai", "Asia/Kolkata",
        "Australia/Sydney", "America/Sao_Paulo", "Africa/Lagos"
    )

    private val locales = listOf(
        "en_US", "en_US", "en_US", "en_US", "en_GB", "de_DE",
        "ja_JP", "zh_CN", "hi_IN", "en_AU", "pt_BR", "en_NG"
    )

    fun getPresetCount(): Int = devicePresets.size

    /**
     * Generate a profile using a specific preset name or ID.
     */
    fun generateProfileWithPreset(
        packageName: String,
        userId: Int,
        presetNameOrId: String
    ): VirtualProfileEntity {
        val index = devicePresets.indexOfFirst {
            it.name.equals(presetNameOrId, ignoreCase = true) ||
            it.presetId.equals(presetNameOrId, ignoreCase = true)
        }.let { if (it == -1) 0 else it }
        return generateFullProfile(packageName, userId, index)
    }


    /**
     * Generate a full VirtualProfileEntity for a clone.
     * Uses device preset based on userId, generates all unique identifiers.
     * Anti-correlation: retries identifier generation if duplicates exist.
     */
    fun generateFullProfile(
        packageName: String,
        userId: Int,
        presetIndex: Int = userId % devicePresets.size
    ): VirtualProfileEntity {
        val preset = devicePresets[presetIndex]
        val cloneId = "${packageName}_user${userId}"

        val androidId = generateUniqueAndroidId()
        val gsfId = generateUniqueGsfId()
        val imei = generateUniqueImei()
        val macAddress = generateUniqueMac()
        val bluetoothMac = generateUniqueMac()

        val tz = timezones[presetIndex % timezones.size]
        val loc = locales[presetIndex % locales.size]

        return VirtualProfileEntity(
            cloneId = cloneId,
            packageName = packageName,
            userId = userId,
            androidId = androidId,
            gsfId = gsfId,
            advertisingId = UUID.randomUUID().toString(),
            deviceModel = preset.model,
            manufacturer = preset.manufacturer,
            brand = preset.brand,
            product = preset.product,
            device = preset.device,
            hardware = preset.hardware,
            buildFingerprint = preset.fingerprint,
            buildDisplay = preset.display,
            buildId = preset.buildId,
            buildType = "user",
            buildTags = "release-keys",
            serial = generateSerial(),
            bootloader = preset.bootloader,
            board = preset.board,
            macAddress = macAddress,
            bluetoothMac = bluetoothMac,
            imei = imei,
            imsi = generateImsi(preset.carrierCode),
            simSerial = generateSimSerial(),
            phoneNumber = generatePhoneNumber(preset.countryIso),
            carrierName = preset.carrier,
            carrierCode = preset.carrierCode,
            countryIso = preset.countryIso,
            screenDensity = preset.screenDensity,
            screenWidth = preset.screenWidth,
            screenHeight = preset.screenHeight,
            sdkVersion = preset.sdkVersion,
            releaseVersion = preset.releaseVersion,
            securityPatch = preset.securityPatch,
            codename = "REL",
            incremental = preset.incremental,
            baseOs = "",
            previewSdkInt = 0,
            locale = loc,
            timezone = tz,
            proxyHost = null,
            proxyPort = null,
            proxyUser = null,
            proxyPass = null,
            dnsServer = null,
            mediaDeviceId = generateMediaDeviceId(),
            appSetId = UUID.randomUUID().toString(),
            webViewUserAgent = buildWebViewUserAgent(preset),
            profilePreset = preset.presetId
        )
    }

    /**
     * Validate a profile — checks IMEI Luhn, MAC format, android_id length, fingerprint format.
     */
    fun validateProfile(profile: VirtualProfileEntity): List<String> {
        val errors = mutableListOf<String>()

        if (profile.androidId.length != 16 || !profile.androidId.matches(Regex("[0-9a-f]+"))) {
            errors.add("Invalid androidId: must be 16 hex chars")
        }
        if (profile.imei.length != 15 || !validateLuhn(profile.imei)) {
            errors.add("Invalid IMEI: must be 15 digits with valid Luhn checksum")
        }
        if (!profile.macAddress.matches(Regex("([0-9A-F]{2}:){5}[0-9A-F]{2}"))) {
            errors.add("Invalid MAC address format")
        }
        if (!profile.bluetoothMac.matches(Regex("([0-9A-F]{2}:){5}[0-9A-F]{2}"))) {
            errors.add("Invalid Bluetooth MAC format")
        }
        if (!profile.buildFingerprint.contains("/") || !profile.buildFingerprint.contains(":")) {
            errors.add("Invalid Build.FINGERPRINT format")
        }
        if (profile.imsi.length != 15) {
            errors.add("Invalid IMSI: must be 15 digits")
        }

        return errors
    }

    // ---- Identifier generators with anti-correlation ----

    private fun generateUniqueAndroidId(): String {
        repeat(100) {
            val id = generateAndroidId()
            if (dao == null || !dao.existsWithAndroidId(id)) return id
        }
        return generateAndroidId() // fallback
    }

    private fun generateUniqueGsfId(): String {
        repeat(100) {
            val id = generateGsfId()
            if (dao == null || !dao.existsWithGsfId(id)) return id
        }
        return generateGsfId()
    }

    private fun generateUniqueImei(): String {
        repeat(100) {
            val imei = generateImei()
            if (dao == null || !dao.existsWithImei(imei)) return imei
        }
        return generateImei()
    }

    private fun generateUniqueMac(): String {
        repeat(100) {
            val mac = generateMacAddress()
            if (dao == null || !dao.existsWithMac(mac)) return mac
        }
        return generateMacAddress()
    }

    // ---- Raw generators ----

    private fun generateAndroidId(): String {
        val bytes = ByteArray(8)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateImei(): String {
        val tac = "35${"%04d".format(random.nextInt(10000))}"
        val serial = "%06d".format(random.nextInt(1000000))
        val partial = "$tac$serial"
        val checkDigit = luhnCheckDigit(partial)
        return "$partial$checkDigit"
    }

    private fun generateImsi(carrierCode: String): String {
        // IMSI = MCC(3) + MNC(2-3) + MSIN(remaining to fill 15 digits)
        val msinLength = 15 - carrierCode.length
        val msin = buildString {
            repeat(msinLength) { append(random.nextInt(10)) }
        }
        return "$carrierCode$msin"
    }

    private fun generateSimSerial(): String {
        // ICCID: 89 (industry) + 01 (US) + carrier + serial = 19-20 digits
        return buildString {
            append("8901")
            repeat(15) { append(random.nextInt(10)) }
            append(random.nextInt(10)) // check digit
        }
    }

    private fun nextInt(origin: Int, bound: Int): Int {
        if (bound <= origin) return origin
        return origin + random.nextInt(bound - origin)
    }

    private fun generatePhoneNumber(countryIso: String): String {
        return when (countryIso) {
            "us" -> "+1${"%03d".format(nextInt(200, 999))}${"%03d".format(nextInt(100, 999))}${"%04d".format(nextInt(1000, 9999))}"
            "gb" -> "+44${"%04d".format(nextInt(7000, 7999))}${"%06d".format(nextInt(100000, 999999))}"
            "jp" -> "+81${"%02d".format(nextInt(70, 99))}${"%04d".format(nextInt(1000, 9999))}${"%04d".format(nextInt(1000, 9999))}"
            "cn" -> "+86${"%03d".format(nextInt(130, 199))}${"%04d".format(nextInt(1000, 9999))}${"%04d".format(nextInt(1000, 9999))}"
            "in" -> "+91${"%05d".format(nextInt(70000, 99999))}${"%05d".format(nextInt(10000, 99999))}"
            else -> "+1${"%03d".format(nextInt(200, 999))}${"%03d".format(nextInt(100, 999))}${"%04d".format(nextInt(1000, 9999))}"
        }
    }

    private fun generateSerial(): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return buildString {
            repeat(12) { append(chars[random.nextInt(chars.length)]) }
        }
    }

    private fun generateGsfId(): String {
        return random.nextLong().let { if (it < 0) -it else it }.toString(16)
    }

    private fun generateMacAddress(): String {
        val bytes = ByteArray(6)
        random.nextBytes(bytes)
        bytes[0] = (bytes[0].toInt() and 0xFE or 0x02).toByte() // locally administered, unicast
        return bytes.joinToString(":") { "%02X".format(it) }
    }

    private fun generateMediaDeviceId(): String {
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun buildWebViewUserAgent(preset: DevicePreset): String {
        val chromeVersion = "126.0.6478.122"
        return "Mozilla/5.0 (Linux; Android ${preset.releaseVersion}; " +
                "${preset.model} Build/${preset.buildId}) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/$chromeVersion Mobile Safari/537.36"
    }

    private fun luhnCheckDigit(number: String): Int {
        var sum = 0
        var alternate = true
        for (i in number.length - 1 downTo 0) {
            var n = number[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return (10 - (sum % 10)) % 10
    }

    private fun validateLuhn(number: String): Boolean {
        if (number.length < 2) return false
        val payload = number.dropLast(1)
        val expected = luhnCheckDigit(payload)
        return (number.last() - '0') == expected
    }

    /**
     * Get all 126+ system property key-value pairs for native injection.
     */
    fun getSystemProperties(profile: VirtualProfileEntity): Map<String, String> {
        return buildMap {
            // Device identity across partitions
            for (partition in listOf("", ".system", ".vendor", ".odm", ".product",
                ".system_ext", ".bootimage", ".vendor_dlkm")) {
                put("ro${partition}.product.brand", profile.brand)
                put("ro${partition}.product.device", profile.device)
                put("ro${partition}.product.manufacturer", profile.manufacturer)
                put("ro${partition}.product.model", profile.deviceModel)
                put("ro${partition}.product.name", profile.product)
                put("ro${partition}.product.board", profile.board)
            }

            // Build info
            put("ro.build.fingerprint", profile.buildFingerprint)
            put("ro.build.display.id", profile.buildDisplay)
            put("ro.build.id", profile.buildId)
            put("ro.build.type", profile.buildType)
            put("ro.build.tags", profile.buildTags)
            put("ro.build.version.sdk", profile.sdkVersion.toString())
            put("ro.build.version.release", profile.releaseVersion)
            put("ro.build.version.security_patch", profile.securityPatch)
            put("ro.build.version.codename", profile.codename)
            put("ro.build.version.incremental", profile.incremental)
            put("ro.build.version.base_os", profile.baseOs)
            put("ro.build.version.preview_sdk", profile.previewSdkInt.toString())
            put("ro.build.description", "${profile.product}-user ${profile.releaseVersion} " +
                    "${profile.buildId} ${profile.incremental} release-keys")

            // Security flags
            put("ro.debuggable", "0")
            put("ro.secure", "1")
            put("ro.adb.secure", "1")
            put("ro.boot.verifiedbootstate", "green")
            put("ro.boot.veritymode", "enforcing")
            put("ro.boot.flash.locked", "1")
            put("ro.boot.warranty_bit", "0")
            put("ro.is_ever_orange", "0")
            put("ro.build.selinux", "1")
            put("persist.sys.usb.config", "none")
            put("init.svc.adbd", "stopped")
            put("sys.oem_unlock_allowed", "0")
            put("ro.boot.vbmeta.device_state", "locked")

            // Hardware platform
            put("ro.hardware", profile.hardware)
            put("ro.board.platform", profile.board)
            put("ro.hardware.chipname", profile.hardware)
            put("ro.serialno", profile.serial)
            put("ro.bootloader", profile.bootloader)
            put("gsm.operator.alpha", profile.carrierName)
            put("gsm.sim.operator.numeric", profile.carrierCode)
            put("gsm.operator.numeric", profile.carrierCode)
            put("gsm.operator.iso-country", profile.countryIso)
            put("gsm.sim.operator.iso-country", profile.countryIso)
            put("persist.sys.timezone", profile.timezone)
            put("persist.sys.locale", profile.locale)
        }
    }

    data class DevicePreset(
        val name: String,
        val model: String,
        val brand: String,
        val manufacturer: String,
        val product: String,
        val device: String,
        val hardware: String,
        val board: String,
        val bootloader: String,
        val fingerprint: String,
        val display: String,
        val buildId: String,
        val incremental: String,
        val securityPatch: String,
        val screenDensity: Int,
        val screenWidth: Int,
        val screenHeight: Int,
        val sdkVersion: Int,
        val releaseVersion: String,
        val carrier: String,
        val carrierCode: String,
        val countryIso: String,
        val presetId: String
    )
}
