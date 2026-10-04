package com.emergent.posapp

import android.app.Application

class PosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServerConfig.appContext = applicationContext
    }
}
