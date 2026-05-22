package com.titanclone.engine.stubs;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Virtual WifiManager proxy — returns spoofed MAC address,
 * SSID, and connection info per clone.
 */
public class WifiStub extends MethodInvocationProxy {

    private static final String TAG = "WifiStub";

    private String virtualMacAddress;
    private String virtualSsid;
    private String virtualBssid;

    @Override
    public String getName() {
        return "WifiManager";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("getMacAddress", this::handleGetMacAddress);
        addMethodHandler("getConnectionInfo", this::handleGetConnectionInfo);

        // TODO: Use reflection to intercept IWifiManager
        markInjected();
    }

    public void setVirtualProfile(String macAddress, String ssid, String bssid) {
        this.virtualMacAddress = macAddress;
        this.virtualSsid = ssid;
        this.virtualBssid = bssid;
    }

    private Object handleGetMacAddress(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualMacAddress != null) {
            Log.d(TAG, "Returning virtual MAC");
            return virtualMacAddress;
        }
        return method.invoke(original, args);
    }

    private Object handleGetConnectionInfo(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Return WifiInfo with virtual MAC/SSID/BSSID
        return method.invoke(original, args);
    }
}
