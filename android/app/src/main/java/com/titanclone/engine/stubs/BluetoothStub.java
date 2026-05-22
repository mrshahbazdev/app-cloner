package com.titanclone.engine.stubs;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Virtual BluetoothAdapter proxy — returns spoofed BT MAC and name per clone.
 */
public class BluetoothStub extends MethodInvocationProxy {

    private static final String TAG = "BTStub";

    private String virtualBtMac;
    private String virtualBtName;

    @Override
    public String getName() {
        return "BluetoothAdapter";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("getAddress", this::handleGetAddress);
        addMethodHandler("getName", this::handleGetName);

        // TODO: Use reflection to intercept IBluetooth
        markInjected();
    }

    public void setVirtualProfile(String btMac, String btName) {
        this.virtualBtMac = btMac;
        this.virtualBtName = btName;
    }

    private Object handleGetAddress(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualBtMac != null) return virtualBtMac;
        return method.invoke(original, args);
    }

    private Object handleGetName(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualBtName != null) return virtualBtName;
        return method.invoke(original, args);
    }
}
