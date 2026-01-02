package com.latefortrain.meshhelper.ui.startscreen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.latefortrain.meshhelper.ui.screens.start.components.PermissionHandlers
import com.latefortrain.meshhelper.ui.startscreen.components.ScannerDialog
import com.latefortrain.meshhelper.viewmodel.GPSViewModel
import com.latefortrain.meshhelper.viewmodel.MeshViewModel
import com.latefortrain.meshhelper.viewmodel.QRViewModel

@Composable
fun StartScreen(
    meshViewModel: MeshViewModel,
    gpsViewModel: GPSViewModel,
    qrViewModel: QRViewModel
) {
    val context = LocalContext.current
    val meshState by meshViewModel.uiState.collectAsState()
    val gpsLocation by gpsViewModel.location.collectAsState()
    val gpsLoading by gpsViewModel.loading.collectAsState()
    val gpsError by gpsViewModel.error.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    // Logic for derived strings
    val locationText = when {
        gpsLoading -> "Fetching location…"
        gpsError != null -> "GPS error: $gpsError"
        gpsLocation != null -> "%.5f, %.5f (acc: %.0fm)".format(gpsLocation!!.latitude, gpsLocation!!.longitude, gpsLocation!!.accuracy)
        else -> "No location yet"
    }

    var triggerCameraLauncher by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Handlers
    PermissionHandlers(
        gpsViewModel = gpsViewModel,
        onLocationPermissionResult = { hasLocationPermission = it },
        onCameraPermissionResult = { granted -> if (granted) showScanner = true },
        onRequireCameraLauncher = { trigger -> triggerCameraLauncher = trigger }
    )

    // For the camera stuff
    // 1. Setup camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
    }

    // 2. Listen for the result to close the dialog automatically
    LaunchedEffect(Unit) {
        qrViewModel.scanResult.collect { qrText ->
            meshViewModel.sendText("/qr_result $qrText")
            showScanner = false // Close the dialog on success
        }
    }

    // 3. The Dialog trigger
    if (showScanner) {
        ScannerDialog(onDismiss = { showScanner = false })
    }

    StartScreenContent(
        meshState = meshState,
        locationText = locationText,
        isGpsReady = gpsLocation != null,
        allPermissionsGranted = hasLocationPermission,
        onTestDistance = {
            gpsLocation?.let { meshViewModel.sendText("/distance ${it.latitude} ${it.longitude}") }
        },
        onTestSignal = { meshViewModel.sendText("/signal") },
        onScanQR = {
            val isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                showScanner = true
            } else {
                // Use the trigger we got from PermissionHandlers
                triggerCameraLauncher?.invoke()
            }
        },
        onRefreshGPS = {
            gpsViewModel.requestFreshLocation { hasLocationPermission }
        }
    )
}