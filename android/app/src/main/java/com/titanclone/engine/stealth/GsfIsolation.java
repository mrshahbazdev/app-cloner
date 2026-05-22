package com.titanclone.engine.stealth;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * GSF (Google Services Framework) ID isolation per clone.
 *
 * Each clone gets a unique GSF ID so Google cannot correlate them
 * to the same device. Intercepts ContentProvider queries to:
 *   content://com.google.android.gsf.gservices
 *
 * Also handles:
 * - GMS device checkin API isolation
 * - Google Account Manager isolation
 * - Settings.Secure google_accounts isolation
 */
public class GsfIsolation {

    private static final String TAG = "GsfIsolation";

    /** GSF Gservices ContentProvider authority */
    public static final String GSF_AUTHORITY = "com.google.android.gsf.gservices";

    /** Key for android_id in GSF */
    public static final String GSF_ANDROID_ID_KEY = "android_id";

    // Map: cloneId -> gsfId
    private final Map<String, String> gsfIds = new HashMap<>();

    // Map: cloneId -> Map<key, value> for all GServices overrides
    private final Map<String, Map<String, String>> gservicesOverrides = new HashMap<>();

    /**
     * Register a GSF ID for a clone.
     */
    public void setGsfId(String cloneId, String gsfId) {
        gsfIds.put(cloneId, gsfId);

        Map<String, String> overrides = gservicesOverrides
                .computeIfAbsent(cloneId, k -> new HashMap<>());
        overrides.put(GSF_ANDROID_ID_KEY, gsfId);

        Log.d(TAG, "GSF ID set for " + cloneId + ": " + gsfId.substring(0, 4) + "***");
    }

    /**
     * Set a GServices override for a clone.
     */
    public void setGservicesOverride(String cloneId, String key, String value) {
        gservicesOverrides.computeIfAbsent(cloneId, k -> new HashMap<>())
                .put(key, value);
    }

    /**
     * Intercept a query to com.google.android.gsf.gservices.
     * Returns clone-specific values if available.
     */
    public Cursor interceptGsfQuery(String cloneId, Uri uri, String[] projection) {
        Map<String, String> overrides = gservicesOverrides.get(cloneId);
        if (overrides == null || overrides.isEmpty()) {
            return null; // let real query through
        }

        // Check if the queried key has an override
        String path = uri.getLastPathSegment();
        if (path != null && overrides.containsKey(path)) {
            MatrixCursor cursor = new MatrixCursor(new String[]{"key", "value"});
            cursor.addRow(new Object[]{path, overrides.get(path)});
            return cursor;
        }

        return null;
    }

    /**
     * Get the GSF ID for a clone, or null if not set.
     */
    public String getGsfId(String cloneId) {
        return gsfIds.get(cloneId);
    }

    /**
     * Remove GSF data for a clone.
     */
    public void clearCloneGsf(String cloneId) {
        gsfIds.remove(cloneId);
        gservicesOverrides.remove(cloneId);
    }

    /**
     * Get all overrides for a clone.
     */
    public Map<String, String> getOverrides(String cloneId) {
        return gservicesOverrides.getOrDefault(cloneId, new HashMap<>());
    }
}
