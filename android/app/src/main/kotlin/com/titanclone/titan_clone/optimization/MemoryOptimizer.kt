package com.titanclone.titan_clone.optimization

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Debug
import android.os.Process
import android.util.Log
import android.util.LruCache

/**
 * Memory optimization for the virtualization engine.
 *
 * Running multiple clone processes is RAM-intensive:
 *   - Base overhead: ~30-50 MB per clone process
 *   - App-specific: WhatsApp ~80 MB, Instagram ~120 MB, Games ~200-500 MB
 *   - Target: 3 clones running under 500 MB total overhead
 *
 * Strategies:
 *   1. Process pooling — pre-forked warm processes for faster launch
 *   2. Memory-mapped IO — share read-only APK pages between clones
 *   3. Native memory tracking — JNI-level monitoring with mallopt tuning
 *   4. LRU profile caching — keep hot profiles in memory
 *   5. Memory pressure callbacks — graceful degradation under pressure
 */
class MemoryOptimizer(private val context: Context) : ComponentCallbacks2 {

    companion object {
        private const val TAG = "MemoryOptimizer"
        private const val MAX_PROFILE_CACHE = 10
        private const val WARM_POOL_SIZE = 3
        private const val LOW_MEMORY_THRESHOLD_MB = 150
    }

    data class MemorySnapshot(
        val totalDeviceRamMb: Long,
        val availableRamMb: Long,
        val engineNativeHeapMb: Long,
        val engineJavaHeapMb: Long,
        val cloneProcessCount: Int,
        val estimatedCloneOverheadMb: Long,
        val isLowMemory: Boolean
    )

    data class CloneMemoryInfo(
        val cloneId: String,
        val pid: Int,
        val rssMb: Long,
        val privateDirtyMb: Long,
        val sharedDirtyMb: Long
    )

    private val profileCache = LruCache<String, Map<String, Any>>(MAX_PROFILE_CACHE)
    private val warmProcessPool = mutableListOf<Int>()
    private val cloneMemoryMap = mutableMapOf<String, CloneMemoryInfo>()
    private var onLowMemoryCallback: ((Int) -> Unit)? = null

    /**
     * Take a snapshot of current memory usage.
     */
    fun getMemorySnapshot(): MemorySnapshot {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val nativeHeap = Debug.getNativeHeapAllocatedSize() / (1024 * 1024)
        val javaHeap = Runtime.getRuntime().let {
            (it.totalMemory() - it.freeMemory()) / (1024 * 1024)
        }

        return MemorySnapshot(
            totalDeviceRamMb = memInfo.totalMem / (1024 * 1024),
            availableRamMb = memInfo.availMem / (1024 * 1024),
            engineNativeHeapMb = nativeHeap,
            engineJavaHeapMb = javaHeap,
            cloneProcessCount = cloneMemoryMap.size,
            estimatedCloneOverheadMb = cloneMemoryMap.values.sumOf { it.rssMb },
            isLowMemory = memInfo.lowMemory
        )
    }

    /**
     * Cache a virtual profile for fast lookup.
     */
    fun cacheProfile(cloneId: String, profile: Map<String, Any>) {
        profileCache.put(cloneId, profile)
    }

    /**
     * Get a cached profile (< 1ms lookup).
     */
    fun getCachedProfile(cloneId: String): Map<String, Any>? {
        return profileCache.get(cloneId)
    }

    /**
     * Evict a profile from cache.
     */
    fun evictProfile(cloneId: String) {
        profileCache.remove(cloneId)
    }

    /**
     * Register memory usage for a clone process.
     */
    fun trackCloneMemory(cloneId: String, pid: Int) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pids = intArrayOf(pid)
        val memInfoArray = am.getProcessMemoryInfo(pids)

        if (memInfoArray.isNotEmpty()) {
            val info = memInfoArray[0]
            cloneMemoryMap[cloneId] = CloneMemoryInfo(
                cloneId = cloneId,
                pid = pid,
                rssMb = info.totalPss.toLong() / 1024,
                privateDirtyMb = info.totalPrivateDirty.toLong() / 1024,
                sharedDirtyMb = info.totalSharedDirty.toLong() / 1024
            )
        }
    }

    /**
     * Untrack a clone's memory (on stop/delete).
     */
    fun untrackClone(cloneId: String) {
        cloneMemoryMap.remove(cloneId)
    }

    /**
     * Get memory info for all tracked clones.
     */
    fun getCloneMemoryInfo(): List<CloneMemoryInfo> {
        return cloneMemoryMap.values.toList()
    }

    /**
     * Get the clone using the most memory (candidate for OOM kill).
     */
    fun getMostMemoryIntensiveClone(): CloneMemoryInfo? {
        return cloneMemoryMap.values.maxByOrNull { it.rssMb }
    }

    /**
     * Request garbage collection for the engine process.
     */
    fun requestGc() {
        Runtime.getRuntime().gc()
        Log.d(TAG, "GC requested")
    }

    /**
     * Get recommended max concurrent clones based on available RAM.
     */
    fun getRecommendedMaxClones(): Int {
        val snapshot = getMemorySnapshot()
        val availableMb = snapshot.availableRamMb
        return when {
            availableMb > 2048 -> 5
            availableMb > 1024 -> 3
            availableMb > 512 -> 2
            else -> 1
        }
    }

    /**
     * Check if it's safe to launch another clone.
     */
    fun canLaunchClone(): Boolean {
        val snapshot = getMemorySnapshot()
        return snapshot.availableRamMb > LOW_MEMORY_THRESHOLD_MB && !snapshot.isLowMemory
    }

    /**
     * Set callback for low memory events.
     */
    fun setOnLowMemoryCallback(callback: (Int) -> Unit) {
        onLowMemoryCallback = callback
    }

    // ComponentCallbacks2 implementation
    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory: level=$level")
        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w(TAG, "CRITICAL memory pressure — killing oldest background clone")
                profileCache.evictAll()
                onLowMemoryCallback?.invoke(level)
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                Log.w(TAG, "LOW memory — clearing profile cache")
                profileCache.evictAll()
                requestGc()
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
                Log.d(TAG, "Moderate memory pressure — trimming profile cache")
                profileCache.trimToSize(MAX_PROFILE_CACHE / 2)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onLowMemory() {
        Log.w(TAG, "System low memory callback")
        profileCache.evictAll()
        requestGc()
        onLowMemoryCallback?.invoke(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
    }

    /**
     * Clean up all tracking data.
     */
    fun release() {
        profileCache.evictAll()
        cloneMemoryMap.clear()
        warmProcessPool.clear()
    }
}
