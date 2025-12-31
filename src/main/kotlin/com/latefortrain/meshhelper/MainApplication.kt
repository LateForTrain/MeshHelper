package com.latefortrain.meshhelper

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.latefortrain.meshhelper.repository.GPSManager
import com.latefortrain.meshhelper.repository.MeshManager

class MainApplication : Application() {
    // This is the single instance shared by the whole app
    lateinit var meshManager: MeshManager
    lateinit var gpsManager: GPSManager

    override fun onCreate() {
        super.onCreate()
        // Pass 'this' (the application context) to prevent memory leaks
        meshManager = MeshManager(this)
        gpsManager = GPSManager(this)

        // Register **once** here — tied to whole process lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(meshManager)
        ProcessLifecycleOwner.get().lifecycle.addObserver(gpsManager)

    }
}