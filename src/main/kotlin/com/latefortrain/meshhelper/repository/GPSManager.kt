package com.latefortrain.meshhelper.repository

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GPSManager(private val context: Context) : DefaultLifecycleObserver {

    private val fused = LocationServices.getFusedLocationProviderClient(context)

    private var isListening = false
    private val _lastLocation = MutableStateFlow<Location?>(null)

    val lastLocation: StateFlow<Location?> = _lastLocation.asStateFlow()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                _lastLocation.value = loc
            }
        }
    }

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    override fun onStart(owner: LifecycleOwner) {
        startPassive()
    }

    override fun onStop(owner: LifecycleOwner) {
        stopPassive()
    }

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    private fun startPassive() {
        if (isListening || !hasPermission()) return

        val request = LocationRequest.Builder(Priority.PRIORITY_PASSIVE, 300_000) // 5 min
            .setMinUpdateIntervalMillis(60_000) // at least 1 min
            .build()

        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
        isListening = true
    }

    private fun stopPassive() {
        if (!isListening) return
        fused.removeLocationUpdates(callback)
        isListening = false
    }

    @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    suspend fun getFreshLocation(): Location? = suspendCancellableCoroutine { cont ->
        if (!hasPermission()) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
            .setMaxUpdateAgeMillis(300_000)  // 5 minutes
            .build()

        val cts = CancellationTokenSource()

        cont.invokeOnCancellation { cts.cancel() }

        // Explicit chain - this should resolve addOnSuccessListener etc.
        fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { location: Location? ->
                location?.let { _lastLocation.value = it }
                cont.resume(location)
            }
            .addOnFailureListener { cont.resume(null) }
            .addOnCanceledListener { cont.cancel() }
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }
}
