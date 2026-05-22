package com.titanclone.engine.stub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Base stub BroadcastReceiver that proxies broadcasts to clone apps.
 * Each process slot has its own StubBroadcastReceiver to ensure
 * broadcasts are received in the correct clone process.
 */
public class StubBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "StubReceiver";
    public static final String EXTRA_REAL_ACTION = "titanclone.extra.REAL_ACTION";
    public static final String EXTRA_CLONE_USER_ID = "titanclone.extra.CLONE_USER_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String realAction = intent.getStringExtra(EXTRA_REAL_ACTION);
        int cloneUserId = intent.getIntExtra(EXTRA_CLONE_USER_ID, -1);

        Log.d(TAG, "Received broadcast for clone user=" + cloneUserId
                + " action=" + realAction);

        // TODO: Route broadcast to clone's registered receivers
    }

    // Process-specific subclasses
    public static class P0 extends StubBroadcastReceiver {}
    public static class P1 extends StubBroadcastReceiver {}
    public static class P2 extends StubBroadcastReceiver {}
    public static class P3 extends StubBroadcastReceiver {}
    public static class P4 extends StubBroadcastReceiver {}
    public static class P5 extends StubBroadcastReceiver {}
    public static class P6 extends StubBroadcastReceiver {}
    public static class P7 extends StubBroadcastReceiver {}
    public static class P8 extends StubBroadcastReceiver {}
    public static class P9 extends StubBroadcastReceiver {}
    public static class P10 extends StubBroadcastReceiver {}
    public static class P11 extends StubBroadcastReceiver {}
    public static class P12 extends StubBroadcastReceiver {}
    public static class P13 extends StubBroadcastReceiver {}
    public static class P14 extends StubBroadcastReceiver {}
    public static class P15 extends StubBroadcastReceiver {}
    public static class P16 extends StubBroadcastReceiver {}
    public static class P17 extends StubBroadcastReceiver {}
    public static class P18 extends StubBroadcastReceiver {}
    public static class P19 extends StubBroadcastReceiver {}
    public static class P20 extends StubBroadcastReceiver {}
    public static class P21 extends StubBroadcastReceiver {}
    public static class P22 extends StubBroadcastReceiver {}
    public static class P23 extends StubBroadcastReceiver {}
    public static class P24 extends StubBroadcastReceiver {}
    public static class P25 extends StubBroadcastReceiver {}
    public static class P26 extends StubBroadcastReceiver {}
    public static class P27 extends StubBroadcastReceiver {}
    public static class P28 extends StubBroadcastReceiver {}
    public static class P29 extends StubBroadcastReceiver {}
}
