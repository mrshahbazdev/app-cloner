package com.titanclone.titan_clone

import android.app.Application
import android.content.Context
import android.util.Log
import com.titanclone.engine.core.VirtualCore

class TitanApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        try {
            VirtualCore.get().doAttachBaseContext(this)
        } catch (e: Throwable) {
            Log.e("TitanApplication", "VirtualCore attach failed", e)
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            VirtualCore.get().doCreate()
        } catch (e: Throwable) {
            Log.e("TitanApplication", "VirtualCore init failed", e)
        }
    }
}
