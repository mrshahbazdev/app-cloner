package com.titanclone.engine.stub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Base stub Service that acts as a proxy for clone app services.
 * Each process slot has its own StubService subclass to ensure
 * the service runs in the correct clone process.
 */
public class StubService extends Service {

    private static final String TAG = "StubService";
    public static final String EXTRA_REAL_INTENT = "titanclone.extra.REAL_SERVICE_INTENT";
    public static final String EXTRA_CLONE_USER_ID = "titanclone.extra.CLONE_USER_ID";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }

        Intent realIntent = intent.getParcelableExtra(EXTRA_REAL_INTENT);
        int cloneUserId = intent.getIntExtra(EXTRA_CLONE_USER_ID, -1);

        if (realIntent != null) {
            Log.d(TAG, "Proxying service for clone user=" + cloneUserId);
            // TODO: Route to clone's actual service implementation
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return clone's service binder
        return null;
    }

    // Process-specific subclasses
    public static class P0 extends StubService {}
    public static class P1 extends StubService {}
    public static class P2 extends StubService {}
    public static class P3 extends StubService {}
    public static class P4 extends StubService {}
    public static class P5 extends StubService {}
    public static class P6 extends StubService {}
    public static class P7 extends StubService {}
    public static class P8 extends StubService {}
    public static class P9 extends StubService {}
    public static class P10 extends StubService {}
    public static class P11 extends StubService {}
    public static class P12 extends StubService {}
    public static class P13 extends StubService {}
    public static class P14 extends StubService {}
    public static class P15 extends StubService {}
    public static class P16 extends StubService {}
    public static class P17 extends StubService {}
    public static class P18 extends StubService {}
    public static class P19 extends StubService {}
    public static class P20 extends StubService {}
    public static class P21 extends StubService {}
    public static class P22 extends StubService {}
    public static class P23 extends StubService {}
    public static class P24 extends StubService {}
    public static class P25 extends StubService {}
    public static class P26 extends StubService {}
    public static class P27 extends StubService {}
    public static class P28 extends StubService {}
    public static class P29 extends StubService {}
}
