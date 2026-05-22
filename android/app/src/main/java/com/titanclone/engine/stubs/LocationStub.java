package com.titanclone.engine.stubs;

import android.location.Location;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Virtual LocationManager proxy — optionally mocks location per clone.
 * Allows each clone to report a different GPS location.
 */
public class LocationStub extends MethodInvocationProxy {

    private static final String TAG = "LocStub";

    private Location virtualLocation;
    private boolean mockEnabled = false;

    @Override
    public String getName() {
        return "LocationManager";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("getLastKnownLocation", this::handleGetLastLocation);
        addMethodHandler("getLastLocation", this::handleGetLastLocation);

        // TODO: Use reflection to intercept ILocationManager
        markInjected();
    }

    public void setMockLocation(double latitude, double longitude) {
        virtualLocation = new Location("virtual");
        virtualLocation.setLatitude(latitude);
        virtualLocation.setLongitude(longitude);
        virtualLocation.setAccuracy(10.0f);
        virtualLocation.setTime(System.currentTimeMillis());
        mockEnabled = true;
    }

    public void clearMockLocation() {
        virtualLocation = null;
        mockEnabled = false;
    }

    private Object handleGetLastLocation(Object original, Method method, Object[] args)
            throws Throwable {
        if (mockEnabled && virtualLocation != null) {
            virtualLocation.setTime(System.currentTimeMillis());
            return virtualLocation;
        }
        return method.invoke(original, args);
    }
}
