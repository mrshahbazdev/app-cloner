package com.titanclone.titan_clone

import android.app.Application
import android.content.Context
import com.titanclone.engine.core.VirtualCore

class TitanApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Early initialization of the virtual engine
        VirtualCore.get().doAttachBaseContext(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Full initialization of virtual subsystems and hooks
        VirtualCore.get().doCreate()
    }
}
