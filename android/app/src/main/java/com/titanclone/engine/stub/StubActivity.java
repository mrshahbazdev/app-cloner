package com.titanclone.engine.stub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Base stub Activity that acts as a proxy for clone app activities.
 * When the engine intercepts startActivity() from a clone, it wraps
 * the real intent inside a StubActivity intent. The StubActivity
 * unwraps and launches the real activity within the clone process.
 *
 * Each process slot (:p0-:p29) has its own StubActivity subclass
 * declared in AndroidManifest.xml to ensure the activity runs in
 * the correct clone process.
 */
public class StubActivity extends Activity {

    private static final String TAG = "StubActivity";
    public static final String EXTRA_REAL_INTENT = "titanclone.extra.REAL_INTENT";
    public static final String EXTRA_CLONE_USER_ID = "titanclone.extra.CLONE_USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent stubIntent = getIntent();
        if (stubIntent == null) {
            finish();
            return;
        }

        Intent realIntent = stubIntent.getParcelableExtra(EXTRA_REAL_INTENT);
        int cloneUserId = stubIntent.getIntExtra(EXTRA_CLONE_USER_ID, -1);

        if (realIntent != null) {
            Log.d(TAG, "Unwrapping real intent for clone user=" + cloneUserId
                    + " component=" + realIntent.getComponent());
            // TODO: Load clone's ClassLoader, inject services, launch real Activity
        } else {
            Log.w(TAG, "No real intent found in stub");
            finish();
        }
    }

    /**
     * Wrap a real intent inside a StubActivity intent for the given process.
     */
    public static Intent wrap(Intent realIntent, int userId, Class<? extends StubActivity> stubClass) {
        Intent stubIntent = new Intent();
        stubIntent.setClass(realIntent.getComponent().getPackageName(), stubClass.getName());
        stubIntent.putExtra(EXTRA_REAL_INTENT, realIntent);
        stubIntent.putExtra(EXTRA_CLONE_USER_ID, userId);
        return stubIntent;
    }

    // Process-specific subclasses (declared in AndroidManifest.xml)
    public static class P0 extends StubActivity {}
    public static class P1 extends StubActivity {}
    public static class P2 extends StubActivity {}
    public static class P3 extends StubActivity {}
    public static class P4 extends StubActivity {}
    public static class P5 extends StubActivity {}
    public static class P6 extends StubActivity {}
    public static class P7 extends StubActivity {}
    public static class P8 extends StubActivity {}
    public static class P9 extends StubActivity {}
    public static class P10 extends StubActivity {}
    public static class P11 extends StubActivity {}
    public static class P12 extends StubActivity {}
    public static class P13 extends StubActivity {}
    public static class P14 extends StubActivity {}
    public static class P15 extends StubActivity {}
    public static class P16 extends StubActivity {}
    public static class P17 extends StubActivity {}
    public static class P18 extends StubActivity {}
    public static class P19 extends StubActivity {}
    public static class P20 extends StubActivity {}
    public static class P21 extends StubActivity {}
    public static class P22 extends StubActivity {}
    public static class P23 extends StubActivity {}
    public static class P24 extends StubActivity {}
    public static class P25 extends StubActivity {}
    public static class P26 extends StubActivity {}
    public static class P27 extends StubActivity {}
    public static class P28 extends StubActivity {}
    public static class P29 extends StubActivity {}
}
