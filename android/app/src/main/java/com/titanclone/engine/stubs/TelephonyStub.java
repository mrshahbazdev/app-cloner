package com.titanclone.engine.stubs;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Virtual Telephony proxy — returns spoofed IMEI, phone number,
 * carrier info per clone from VirtualProfile.
 *
 * Hooked methods:
 * - getDeviceId() / getImei() / getMeid() — return virtual IMEI
 * - getSubscriberId() — return virtual IMSI
 * - getSimSerialNumber() — return virtual SIM serial
 * - getLine1Number() — return virtual phone number
 * - getSimOperatorName() / getSimOperator() — return virtual carrier
 * - getNetworkOperatorName() / getNetworkOperator()
 */
public class TelephonyStub extends MethodInvocationProxy {

    private static final String TAG = "TelStub";

    // Per-clone telephony values (set by VirtualProfileManager)
    private String virtualImei;
    private String virtualImsi;
    private String virtualSimSerial;
    private String virtualPhoneNumber;
    private String virtualCarrierName;
    private String virtualCarrierCode;

    @Override
    public String getName() {
        return "TelephonyManager";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("getDeviceId", this::handleGetDeviceId);
        addMethodHandler("getImei", this::handleGetDeviceId);
        addMethodHandler("getMeid", this::handleGetDeviceId);
        addMethodHandler("getSubscriberId", this::handleGetSubscriberId);
        addMethodHandler("getSimSerialNumber", this::handleGetSimSerial);
        addMethodHandler("getLine1Number", this::handleGetPhoneNumber);
        addMethodHandler("getSimOperatorName", this::handleGetCarrierName);
        addMethodHandler("getSimOperator", this::handleGetCarrierCode);
        addMethodHandler("getNetworkOperatorName", this::handleGetCarrierName);
        addMethodHandler("getNetworkOperator", this::handleGetCarrierCode);
        addMethodHandler("getSimCountryIso", this::handleGetSimCountry);
        addMethodHandler("getNetworkCountryIso", this::handleGetSimCountry);

        // TODO: Use reflection to intercept ITelephony via ServiceManager
        markInjected();
    }

    public void setVirtualProfile(String imei, String imsi, String simSerial,
                                   String phoneNumber, String carrierName,
                                   String carrierCode) {
        this.virtualImei = imei;
        this.virtualImsi = imsi;
        this.virtualSimSerial = simSerial;
        this.virtualPhoneNumber = phoneNumber;
        this.virtualCarrierName = carrierName;
        this.virtualCarrierCode = carrierCode;
    }

    private Object handleGetDeviceId(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualImei != null) {
            Log.d(TAG, "Returning virtual IMEI");
            return virtualImei;
        }
        return method.invoke(original, args);
    }

    private Object handleGetSubscriberId(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualImsi != null) return virtualImsi;
        return method.invoke(original, args);
    }

    private Object handleGetSimSerial(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualSimSerial != null) return virtualSimSerial;
        return method.invoke(original, args);
    }

    private Object handleGetPhoneNumber(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualPhoneNumber != null) return virtualPhoneNumber;
        return method.invoke(original, args);
    }

    private Object handleGetCarrierName(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualCarrierName != null) return virtualCarrierName;
        return method.invoke(original, args);
    }

    private Object handleGetCarrierCode(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualCarrierCode != null) return virtualCarrierCode;
        return method.invoke(original, args);
    }

    private Object handleGetSimCountry(Object original, Method method, Object[] args)
            throws Throwable {
        // Default to US if no profile set
        return "us";
    }
}
