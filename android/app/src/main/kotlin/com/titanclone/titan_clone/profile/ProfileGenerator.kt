package com.titanclone.titan_clone.profile

import java.security.SecureRandom
import java.util.UUID

class ProfileGenerator {

    private val random = SecureRandom()

    private val devicePresets = listOf(
        DevicePreset("Google Pixel 8 Pro", "husky", "google", "Google",
            "google/husky/husky:14/AP2A.240805.005/12025142:user/release-keys",
            420, 1344, 2992, 34, "14"),
        DevicePreset("Samsung Galaxy S24 Ultra", "SM-S928B", "samsung", "samsung",
            "samsung/dm3q/dm3q:14/UP1A.231005.007/S928BXXS1AXB1:user/release-keys",
            480, 1440, 3120, 34, "14"),
        DevicePreset("OnePlus 12", "CPH2583", "OnePlus", "OnePlus",
            "OnePlus/CPH2583/OP5913L1:14/UKQ1.230924.001/1704180000:user/release-keys",
            480, 1440, 3168, 34, "14"),
        DevicePreset("Xiaomi 14 Pro", "23116PN5BC", "Xiaomi", "Xiaomi",
            "Xiaomi/missi/missi:14/UKQ1.231003.002/V816.0.3.0.UNACNXM:user/release-keys",
            480, 1440, 3200, 34, "14"),
        DevicePreset("Google Pixel 7", "panther", "google", "Google",
            "google/panther/panther:14/AP2A.240805.005/12025142:user/release-keys",
            420, 1080, 2400, 34, "14"),
        DevicePreset("Samsung Galaxy A54", "SM-A546B", "samsung", "samsung",
            "samsung/a54xnsxx/a54x:14/UP1A.231005.007/A546BXXS7CXA2:user/release-keys",
            393, 1080, 2340, 34, "14"),
        DevicePreset("Sony Xperia 1 V", "XQ-DQ72", "Sony", "Sony",
            "Sony/XQ-DQ72/XQ-DQ72:14/67.2.A.2.45/067002A002004500:user/release-keys",
            480, 1644, 3840, 34, "14"),
        DevicePreset("Nothing Phone (2)", "A065", "Nothing", "Nothing",
            "Nothing/Pong/Pong:14/UKQ1.230924.001/2401100141:user/release-keys",
            420, 1080, 2412, 34, "14"),
        DevicePreset("Motorola Edge 40 Pro", "XT2301-4", "motorola", "Motorola",
            "motorola/eqs_g/eqs:14/U1TQS34.66-18-2-8/14af62:user/release-keys",
            393, 1080, 2400, 34, "14"),
        DevicePreset("Oppo Find X7 Ultra", "PHZ110", "OPPO", "OPPO",
            "OPPO/PHZ110/OP5B11L1:14/UKQ1.230924.001/1704180000:user/release-keys",
            480, 1440, 3168, 34, "14"),
        DevicePreset("Realme GT 5 Pro", "RMX3888", "realme", "realme",
            "realme/RMX3888/RE58C2L1:14/UKQ1.230924.001/1704180000:user/release-keys",
            480, 1264, 2780, 34, "14"),
        DevicePreset("Vivo X100 Pro", "V2324A", "vivo", "vivo",
            "vivo/V2324A/V2324A:14/UKQ1.230924.001/1704180000:user/release-keys",
            480, 1440, 3200, 34, "14")
    )

    fun generateProfile(userId: Int): Map<String, Any> {
        val preset = devicePresets[userId % devicePresets.size]

        return mapOf(
            "id" to UUID.randomUUID().toString(),
            "name" to preset.name,
            "model" to preset.model,
            "brand" to preset.brand,
            "manufacturer" to preset.manufacturer,
            "fingerprint" to preset.fingerprint,
            "screenDensity" to preset.screenDensity,
            "screenWidth" to preset.screenWidth,
            "screenHeight" to preset.screenHeight,
            "abis" to listOf("arm64-v8a", "armeabi-v7a"),
            "sdkVersion" to preset.sdkVersion,
            "releaseVersion" to preset.releaseVersion,
            "androidId" to generateAndroidId(),
            "imei" to generateImei(),
            "macAddress" to generateMacAddress(),
            "bluetoothMac" to generateMacAddress(),
            "gsfId" to generateGsfId(),
            "advertisingId" to UUID.randomUUID().toString()
        )
    }

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

    private fun generateMacAddress(): String {
        val bytes = ByteArray(6)
        random.nextBytes(bytes)
        bytes[0] = (bytes[0].toInt() and 0xFE or 0x02).toByte() // locally administered, unicast
        return bytes.joinToString(":") { "%02X".format(it) }
    }

    private fun generateGsfId(): String {
        return random.nextLong().let { if (it < 0) -it else it }.toString(16)
    }

    data class DevicePreset(
        val name: String,
        val model: String,
        val brand: String,
        val manufacturer: String,
        val fingerprint: String,
        val screenDensity: Int,
        val screenWidth: Int,
        val screenHeight: Int,
        val sdkVersion: Int,
        val releaseVersion: String
    )
}
