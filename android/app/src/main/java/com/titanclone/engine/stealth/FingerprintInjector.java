package com.titanclone.engine.stealth;

import android.util.Log;

import com.titanclone.engine.stubs.AccountManagerStub;
import com.titanclone.engine.stubs.BluetoothStub;
import com.titanclone.engine.stubs.CameraStub;
import com.titanclone.engine.stubs.ClipboardStub;
import com.titanclone.engine.stubs.LocationStub;
import com.titanclone.engine.stubs.NotificationStub;
import com.titanclone.engine.stubs.SettingsStub;
import com.titanclone.engine.stubs.StubManager;
import com.titanclone.engine.stubs.TelephonyStub;
import com.titanclone.engine.stubs.WifiStub;

/**
 * Wires virtual profile values into all system service stubs.
 *
 * Called after a clone profile is loaded, this class pushes the
 * spoofed identifiers into each of the 14 service stubs so that
 * all API calls from the clone return consistent fake values.
 *
 * Also triggers Build.* field injection and native property injection.
 */
public class FingerprintInjector {

    private static final String TAG = "FingerprintInjector";

    /**
     * Inject a complete virtual profile into all subsystems.
     *
     * @param androidId     16-char hex android_id
     * @param gsfId         GSF ID (hex long)
     * @param model         Build.MODEL
     * @param manufacturer  Build.MANUFACTURER
     * @param brand         Build.BRAND
     * @param product       Build.PRODUCT
     * @param device        Build.DEVICE
     * @param hardware      Build.HARDWARE
     * @param fingerprint   Build.FINGERPRINT
     * @param display       Build.DISPLAY
     * @param buildId       Build.ID
     * @param type          Build.TYPE
     * @param tags          Build.TAGS
     * @param serial        Build.SERIAL
     * @param bootloader    Build.BOOTLOADER
     * @param board         Build.BOARD
     * @param imei          15-digit IMEI
     * @param imsi          15-digit IMSI
     * @param simSerial     SIM serial
     * @param phoneNumber   Phone number
     * @param carrierName   Carrier display name
     * @param carrierCode   MCC+MNC
     * @param countryIso    Country ISO code
     * @param macAddress    WiFi MAC
     * @param bluetoothMac  BT MAC
     * @param sdkInt        Build.VERSION.SDK_INT
     * @param release       Build.VERSION.RELEASE
     * @param securityPatch Build.VERSION.SECURITY_PATCH
     * @param codename      Build.VERSION.CODENAME
     * @param incremental   Build.VERSION.INCREMENTAL
     * @param baseOs        Build.VERSION.BASE_OS
     * @param previewSdkInt Build.VERSION.PREVIEW_SDK_INT
     * @param cloneTag      Notification tag for this clone
     */
    public static void injectFullProfile(
            String androidId, String gsfId,
            String model, String manufacturer, String brand,
            String product, String device, String hardware,
            String fingerprint, String display, String buildId,
            String type, String tags, String serial,
            String bootloader, String board,
            String imei, String imsi, String simSerial,
            String phoneNumber, String carrierName, String carrierCode,
            String countryIso,
            String macAddress, String bluetoothMac,
            int sdkInt, String release, String securityPatch,
            String codename, String incremental, String baseOs,
            int previewSdkInt,
            String cloneTag) {

        // 1. Inject Build.* static fields
        BuildFieldInjector.inject(
                model, manufacturer, brand, product, device, hardware,
                fingerprint, display, buildId, type, tags, serial,
                bootloader, board,
                sdkInt, release, securityPatch, codename, incremental,
                baseOs, previewSdkInt);

        // 2. Wire values into service stubs
        StubManager stubManager = StubManager.get();

        // TelephonyStub
        TelephonyStub telephony = stubManager.getStub(TelephonyStub.class);
        if (telephony != null) {
            telephony.setVirtualProfile(imei, imsi, simSerial,
                    phoneNumber, carrierName, carrierCode);
        }

        // WifiStub
        WifiStub wifi = stubManager.getStub(WifiStub.class);
        if (wifi != null) {
            wifi.setVirtualProfile(macAddress, "TitanClone-AP", "00:11:22:33:44:55");
        }

        // BluetoothStub
        BluetoothStub bluetooth = stubManager.getStub(BluetoothStub.class);
        if (bluetooth != null) {
            bluetooth.setVirtualProfile(bluetoothMac, model);
        }

        // SettingsStub (android_id, gsf_id)
        SettingsStub settings = stubManager.getStub(SettingsStub.class);
        if (settings != null) {
            settings.setAndroidId(androidId);
            settings.setVirtualSetting("bluetooth_address", bluetoothMac);
            settings.setVirtualSetting("wifi_mac_address", macAddress);
        }

        // NotificationStub
        NotificationStub notification = stubManager.getStub(NotificationStub.class);
        if (notification != null) {
            notification.setCloneTag(cloneTag);
        }

        Log.i(TAG, "Full profile injected: model=" + model
                + " imei=" + imei.substring(0, 4) + "***"
                + " androidId=" + androidId.substring(0, 4) + "***");
    }
}
