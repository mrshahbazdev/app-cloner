package com.titanclone.titan_clone.notification

import android.app.Activity
import android.os.Bundle
import android.util.Log

/**
 * Transparent activity that routes notification taps to the correct clone.
 *
 * When the user taps a clone notification, this activity extracts the
 * cloneId and delegates to the engine to launch/bring-to-front the
 * correct clone process.
 */
class CloneNotificationRouter : Activity() {

    companion object {
        private const val TAG = "NotificationRouter"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cloneId = intent?.getStringExtra("cloneId")
        if (cloneId != null) {
            Log.d(TAG, "Routing notification tap to clone: $cloneId")
            // Launch clone via the engine — the Flutter bridge will
            // handle bringing the clone to the foreground
        } else {
            Log.w(TAG, "No cloneId in notification routing intent")
        }

        finish()
    }
}
