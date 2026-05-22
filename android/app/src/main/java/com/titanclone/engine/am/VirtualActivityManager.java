package com.titanclone.engine.am;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Virtual Activity Manager — tracks virtual activities, tasks, and
 * back stacks for each clone process.
 *
 * TODO: Implement full virtual activity lifecycle management.
 * Must handle:
 * - Activity launch routing to correct :pN process
 * - Back stack management per clone
 * - Task affinity isolation
 * - Recent tasks spoofing
 */
public class VirtualActivityManager {

    private static final String TAG = "VirtualActivityManager";

    private final Map<String, ActivityRecord> activityRecords = new HashMap<>();

    /**
     * Record a virtual activity launch.
     */
    public void onActivityLaunched(String cloneId, String activityName) {
        activityRecords.put(
            cloneId + "/" + activityName,
            new ActivityRecord(cloneId, activityName, System.currentTimeMillis())
        );
        Log.d(TAG, "Activity launched: " + activityName + " in clone: " + cloneId);
    }

    /**
     * Record a virtual activity destruction.
     */
    public void onActivityDestroyed(String cloneId, String activityName) {
        activityRecords.remove(cloneId + "/" + activityName);
        Log.d(TAG, "Activity destroyed: " + activityName + " in clone: " + cloneId);
    }

    /**
     * Get active activity count for a clone.
     */
    public int getActivityCount(String cloneId) {
        int count = 0;
        for (String key : activityRecords.keySet()) {
            if (key.startsWith(cloneId + "/")) {
                count++;
            }
        }
        return count;
    }

    private static class ActivityRecord {
        final String cloneId;
        final String activityName;
        final long launchTime;

        ActivityRecord(String cloneId, String activityName, long launchTime) {
            this.cloneId = cloneId;
            this.activityName = activityName;
            this.launchTime = launchTime;
        }
    }
}
