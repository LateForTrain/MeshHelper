package com.latefortrain.meshhelper.ui.screens.start.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.latefortrain.meshhelper.viewmodel.GPSViewModel

@Composable
fun PermissionHandlers(
    gpsViewModel: GPSViewModel,
    onLocationPermissionResult: (Boolean) -> Unit,
    onCameraPermissionResult: (Boolean) -> Unit,
    onRequireCameraLauncher: (() -> Unit) -> Unit
) {
    // --- GPS Launcher ---
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onLocationPermissionResult(granted)
        if (granted) gpsViewModel.requestFreshLocation { true }
    }

    // --- Camera Launcher ---
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onCameraPermissionResult(granted)
    }

    // --- Wire up the triggers ---
    LaunchedEffect(Unit) {
        // 1. Listen for GPS VM events
        gpsViewModel.events.collect { event ->
            if (event is GPSViewModel.UiEvent.RequestPermission) {
                locationLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        // 2. Export the camera launcher to the UI
        onRequireCameraLauncher {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}