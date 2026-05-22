package com.titanclone.engine.process;

import android.util.Log;

import com.titanclone.engine.core.EngineConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages clone processes (:p0, :p1, ... :pN).
 * Handles process allocation, lifecycle, and warm process pooling.
 *
 * TODO: Implement process forking, warm pool, and OOM management.
 */
public class ProcessManager {

    private static final String TAG = "ProcessManager";

    private final Map<String, Integer> activeClones = new HashMap<>();
    private int nextProcessIndex = 0;

    /**
     * Allocate a process slot for a new clone.
     *
     * @return process index (e.g., 0 for :p0, 1 for :p1)
     */
    public int allocateProcess(String cloneId) {
        if (activeClones.size() >= EngineConfig.MAX_CLONE_PROCESSES) {
            Log.w(TAG, "Max clone processes reached: " + EngineConfig.MAX_CLONE_PROCESSES);
            return -1;
        }

        int processIndex = nextProcessIndex++;
        activeClones.put(cloneId, processIndex);
        Log.d(TAG, "Allocated process :p" + processIndex + " for clone: " + cloneId);
        return processIndex;
    }

    /**
     * Release a process slot when a clone is stopped or deleted.
     */
    public void releaseProcess(String cloneId) {
        Integer index = activeClones.remove(cloneId);
        if (index != null) {
            Log.d(TAG, "Released process :p" + index + " for clone: " + cloneId);
        }
    }

    /**
     * Get the process name for a clone.
     */
    public String getProcessName(String cloneId) {
        Integer index = activeClones.get(cloneId);
        if (index == null) return null;
        return EngineConfig.CLONE_PROCESS_PREFIX + index;
    }

    /**
     * Check if a clone has an active process.
     */
    public boolean isProcessActive(String cloneId) {
        return activeClones.containsKey(cloneId);
    }

    /**
     * Get count of active clone processes.
     */
    public int getActiveProcessCount() {
        return activeClones.size();
    }
}
