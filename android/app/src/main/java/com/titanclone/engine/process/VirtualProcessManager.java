package com.titanclone.engine.process;

import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import com.titanclone.engine.core.EngineConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages clone process lifecycle — allocation, monitoring,
 * OOM priority, death detection, and graceful shutdown.
 *
 * Extends the basic ProcessManager with:
 * - Process slot recycling (reuse freed :pN slots)
 * - Binder death detection (linkToDeath)
 * - OOM score management for foreground/background clones
 * - Warm process pool for fast clone launch
 */
public class VirtualProcessManager {

    private static final String TAG = "VirtualProcessMgr";

    private static volatile VirtualProcessManager sInstance;

    // Map: processIndex -> CloneProcessRecord
    private final Map<Integer, CloneProcessRecord> processSlots = new HashMap<>();

    // Available process indices (freed slots)
    private final Set<Integer> availableSlots = new HashSet<>();

    // Map: cloneId -> processIndex
    private final Map<String, Integer> cloneToProcess = new HashMap<>();

    // Next process index for new allocations
    private int nextIndex = 0;

    // Listeners for process state changes
    private ProcessStateListener listener;

    private VirtualProcessManager() {
        // Initialize available slots
        for (int i = 0; i < EngineConfig.MAX_CLONE_PROCESSES; i++) {
            availableSlots.add(i);
        }
    }

    public static VirtualProcessManager get() {
        if (sInstance == null) {
            synchronized (VirtualProcessManager.class) {
                if (sInstance == null) {
                    sInstance = new VirtualProcessManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * Allocate a process slot for a clone and start the process.
     *
     * @return CloneProcessRecord with assigned slot, or null if no slots available
     */
    public synchronized CloneProcessRecord allocateProcess(
            String cloneId, String packageName, int userId) {

        // Check if already allocated
        if (cloneToProcess.containsKey(cloneId)) {
            int existingIdx = cloneToProcess.get(cloneId);
            return processSlots.get(existingIdx);
        }

        // Find available slot
        if (availableSlots.isEmpty()) {
            Log.w(TAG, "No process slots available (max=" + EngineConfig.MAX_CLONE_PROCESSES + ")");
            return null;
        }

        int slotIndex = availableSlots.iterator().next();
        availableSlots.remove(slotIndex);

        CloneProcessRecord record = new CloneProcessRecord(
                cloneId, packageName, userId, slotIndex);
        record.state = ProcessState.STARTING;

        processSlots.put(slotIndex, record);
        cloneToProcess.put(cloneId, slotIndex);

        Log.i(TAG, "Allocated :p" + slotIndex + " for " + cloneId);

        // TODO: Actually start the process by triggering the StubContentProvider
        // This will be done by querying the stub provider's authority:
        //   content://com.titanclone.titan_clone.stub.provider.pN
        // ContentProvider.onCreate() runs first, initializing the clone environment.

        return record;
    }

    /**
     * Release a process slot when a clone stops.
     */
    public synchronized void releaseProcess(String cloneId) {
        Integer slotIndex = cloneToProcess.remove(cloneId);
        if (slotIndex == null) return;

        CloneProcessRecord record = processSlots.remove(slotIndex);
        availableSlots.add(slotIndex);

        if (record != null) {
            record.state = ProcessState.DEAD;
            Log.i(TAG, "Released :p" + slotIndex + " for " + cloneId);

            if (listener != null) {
                listener.onProcessDied(cloneId, slotIndex);
            }
        }
    }

    /**
     * Force-stop a clone process.
     */
    public synchronized void forceStop(String cloneId) {
        Integer slotIndex = cloneToProcess.get(cloneId);
        if (slotIndex == null) return;

        CloneProcessRecord record = processSlots.get(slotIndex);
        if (record != null && record.pid > 0) {
            Log.w(TAG, "Force-stopping :p" + slotIndex + " pid=" + record.pid);
            Process.killProcess(record.pid);
        }

        releaseProcess(cloneId);
    }

    /**
     * Update OOM adj score for a clone process.
     * Lower score = less likely to be killed.
     *
     * @param foreground true if clone is visible/foreground
     */
    public synchronized void setProcessPriority(String cloneId, boolean foreground) {
        Integer slotIndex = cloneToProcess.get(cloneId);
        if (slotIndex == null) return;

        CloneProcessRecord record = processSlots.get(slotIndex);
        if (record == null) return;

        record.isForeground = foreground;
        record.state = foreground ? ProcessState.FOREGROUND : ProcessState.BACKGROUND;

        // TODO: Use reflection to set oom_adj via ProcessList
        // On rooted devices or with system privileges, we can use:
        //   Process.setOomAdj(pid, foreground ? 0 : 900)
        Log.d(TAG, "Priority set for :p" + slotIndex + " foreground=" + foreground);
    }

    /**
     * Register a Binder death recipient for a clone's process.
     */
    public void linkToDeath(String cloneId, IBinder binder) {
        try {
            binder.linkToDeath(() -> {
                Log.w(TAG, "Process died for clone: " + cloneId);
                releaseProcess(cloneId);
            }, 0);
        } catch (Exception e) {
            Log.e(TAG, "Failed to link to death for " + cloneId, e);
        }
    }

    /**
     * Get process record for a clone.
     */
    public synchronized CloneProcessRecord getProcessRecord(String cloneId) {
        Integer slotIndex = cloneToProcess.get(cloneId);
        if (slotIndex == null) return null;
        return processSlots.get(slotIndex);
    }

    /**
     * Check if a clone has an active process.
     */
    public synchronized boolean isProcessAlive(String cloneId) {
        CloneProcessRecord record = getProcessRecord(cloneId);
        return record != null && record.state != ProcessState.DEAD;
    }

    /**
     * Get count of active processes.
     */
    public synchronized int getActiveCount() {
        return processSlots.size();
    }

    /**
     * Get count of available slots.
     */
    public synchronized int getAvailableSlots() {
        return availableSlots.size();
    }

    /**
     * Kill all clone processes (app shutting down).
     */
    public synchronized void killAll() {
        for (CloneProcessRecord record : processSlots.values()) {
            if (record.pid > 0) {
                Process.killProcess(record.pid);
            }
        }
        processSlots.clear();
        cloneToProcess.clear();
        availableSlots.clear();
        for (int i = 0; i < EngineConfig.MAX_CLONE_PROCESSES; i++) {
            availableSlots.add(i);
        }
        Log.i(TAG, "All clone processes killed");
    }

    public void setProcessStateListener(ProcessStateListener listener) {
        this.listener = listener;
    }

    /**
     * Record of a running clone process.
     */
    public static class CloneProcessRecord {
        public final String cloneId;
        public final String packageName;
        public final int userId;
        public final int processIndex;
        public int pid;
        public ProcessState state;
        public boolean isForeground;
        public long startTime;

        CloneProcessRecord(String cloneId, String packageName, int userId, int processIndex) {
            this.cloneId = cloneId;
            this.packageName = packageName;
            this.userId = userId;
            this.processIndex = processIndex;
            this.startTime = System.currentTimeMillis();
        }

        public String getProcessName() {
            return EngineConfig.CLONE_PROCESS_PREFIX + processIndex;
        }
    }

    public enum ProcessState {
        STARTING,
        RUNNING,
        FOREGROUND,
        BACKGROUND,
        DEAD
    }

    public interface ProcessStateListener {
        void onProcessDied(String cloneId, int processIndex);
    }
}
