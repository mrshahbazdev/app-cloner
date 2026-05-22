package com.titanclone.engine.stubs;

import android.util.Log;

/**
 * Virtual Telephony proxy — returns spoofed IMEI, phone number,
 * carrier info per clone.
 *
 * TODO: Implement ITelephony proxy for device identity spoofing.
 */
public class TelephonyStub {

    private static final String TAG = "TelephonyStub";

    /**
     * Hook getDeviceId (IMEI) to return clone-specific value.
     */
    public static void hookGetDeviceId() {
        // TODO: Intercept TelephonyManager.getDeviceId()
        // Return the IMEI from clone's VirtualProfile.
        Log.d(TAG, "Telephony getDeviceId hook registered");
    }

    /**
     * Hook getSubscriberId (IMSI) to return clone-specific value.
     */
    public static void hookGetSubscriberId() {
        Log.d(TAG, "Telephony getSubscriberId hook registered");
    }

    /**
     * Hook getSimOperatorName to return clone-specific carrier.
     */
    public static void hookGetSimOperator() {
        Log.d(TAG, "Telephony getSimOperator hook registered");
    }
}
