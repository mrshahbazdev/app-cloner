package com.titanclone.engine.ipc;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.titanclone.engine.core.EngineConfig;
import com.titanclone.engine.pm.VirtualPackageManager;
import com.titanclone.engine.stub.StubActivity;

/**
 * Routes intents between clone processes, the virtual server,
 * and the host system.
 *
 * Intent routing rules:
 * 1. Internal: Clone A -> Clone A component => route directly
 * 2. Cross-clone: Clone A -> Clone B component => route via virtual server
 * 3. External: Clone -> real app => unwrap and pass to real system
 * 4. Incoming: Real system -> Clone => wrap in stub and route to :pN
 */
public class IntentRouter {

    private static final String TAG = "IntentRouter";

    private final VirtualPackageManager virtualPm;
    private int currentUserId = -1;

    public IntentRouter(VirtualPackageManager virtualPm) {
        this.virtualPm = virtualPm;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    /**
     * Rewrite an outgoing intent from a clone process.
     * Determines if the target is internal, cross-clone, or external.
     */
    public Intent rewriteOutgoingIntent(Intent original) {
        if (original == null) return null;

        ComponentName target = original.getComponent();
        if (target == null) {
            // Implicit intent — let the system resolve it
            // TODO: Check if any virtual package can handle it
            return tagWithCloneId(original);
        }

        String targetPackage = target.getPackageName();

        // Check if target is a virtually installed package
        if (virtualPm.isPackageInstalled(targetPackage, currentUserId)) {
            Log.d(TAG, "Internal route: " + target.flattenToShortString());
            return wrapForInternalRouting(original);
        }

        // External target — pass through to real system
        Log.d(TAG, "External route: " + target.flattenToShortString());
        return tagWithCloneId(original);
    }

    /**
     * Rewrite an incoming intent destined for a clone.
     * Wraps it in a StubActivity intent for the correct :pN process.
     */
    public Intent rewriteIncomingIntent(Intent original, int processIndex, int userId) {
        Intent stubIntent = new Intent();
        // TODO: Set component to correct StubActivity.P{processIndex}
        stubIntent.putExtra(StubActivity.EXTRA_REAL_INTENT, original);
        stubIntent.putExtra(StubActivity.EXTRA_CLONE_USER_ID, userId);
        return stubIntent;
    }

    /**
     * Check if a broadcast should be delivered to a clone.
     * Only broadcasts matching the clone's user space are delivered.
     */
    public boolean shouldDeliverBroadcast(Intent broadcast, int userId) {
        String action = broadcast.getAction();
        if (action == null) return false;

        // System broadcasts are always delivered
        if (isSystemBroadcast(action)) return true;

        // Check if broadcast is tagged for this user
        int targetUser = broadcast.getIntExtra(StubActivity.EXTRA_CLONE_USER_ID, -1);
        return targetUser == -1 || targetUser == userId;
    }

    /**
     * Rewrite a PendingIntent so it routes back to the correct clone.
     */
    public Intent tagPendingIntent(Intent intent, int userId, int processIndex) {
        intent.putExtra(StubActivity.EXTRA_CLONE_USER_ID, userId);
        intent.putExtra("titanclone.extra.PROCESS_INDEX", processIndex);
        return intent;
    }

    private Intent wrapForInternalRouting(Intent original) {
        Intent wrapped = new Intent(original);
        wrapped.putExtra(StubActivity.EXTRA_CLONE_USER_ID, currentUserId);
        wrapped.putExtra("titanclone.extra.INTERNAL_ROUTE", true);
        return wrapped;
    }

    private Intent tagWithCloneId(Intent intent) {
        intent.putExtra(StubActivity.EXTRA_CLONE_USER_ID, currentUserId);
        return intent;
    }

    private boolean isSystemBroadcast(String action) {
        return action.startsWith("android.intent.action.")
                || action.startsWith("android.net.")
                || action.startsWith("android.bluetooth.")
                || action.equals(Intent.ACTION_SCREEN_ON)
                || action.equals(Intent.ACTION_SCREEN_OFF)
                || action.equals(Intent.ACTION_BATTERY_CHANGED)
                || action.equals(Intent.ACTION_TIME_TICK)
                || action.equals(Intent.ACTION_TIMEZONE_CHANGED)
                || action.equals(Intent.ACTION_LOCALE_CHANGED);
    }
}
