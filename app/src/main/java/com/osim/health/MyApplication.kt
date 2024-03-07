package com.osim.health

import android.app.Application
import com.osim.health.model.ObjectBox

class MyApplication : Application() {
    companion object {
        lateinit var App: Application
    }

    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
        App = this
    }
}