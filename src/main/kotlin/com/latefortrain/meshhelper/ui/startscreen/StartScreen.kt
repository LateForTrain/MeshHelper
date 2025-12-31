package com.latefortrain.meshhelper.ui.startscreen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.latefortrain.meshhelper.viewmodel.GPSViewModel
import com.latefortrain.meshhelper.viewmodel.MeshViewModel

@Composable
fun StartScreen(meshViewModel: MeshViewModel, gpsViewModel: GPSViewModel) {
    val context = LocalContext.current

    // 1. Permission state (simple booleans – you can combine them)
    var hasFineLocation by remember { mutableStateOf(false) }
    var hasCoarseLocation by remember { mutableStateOf(false) }

    val allPermissionsGranted by remember {
        derivedStateOf { hasFineLocation || hasCoarseLocation }
    }

    // 2. Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasFineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        hasCoarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        // Optional: if now granted → trigger fresh location
        if (hasFineLocation || hasCoarseLocation) {
            gpsViewModel.requestFreshLocation { true }  // already granted
        }
    }

    // 3. React to ViewModel events (request permission when needed)
    LaunchedEffect(Unit) {
        gpsViewModel.events.collect { event ->
            when (event) {
                GPSViewModel.UiEvent.RequestPermission -> {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }
    }

    val meshState by meshViewModel.uiState.collectAsState()
    val gpsLocation by gpsViewModel.location.collectAsState()
    val gpsLoading by gpsViewModel.loading.collectAsState()
    val gpsError by gpsViewModel.error.collectAsState()

    // Simple way to consider GPS "ready": we have a non-null location
    val isGpsReady = gpsLocation != null

    // Button enabled only when both are good
    val canSendDistance = meshState.isServiceBound && isGpsReady

    // Format location nicely (or fallback message)
    val locationText = when {
        gpsLoading -> "Fetching location…"
        gpsError != null -> "GPS error: $gpsError"
        gpsLocation != null -> {
            val loc = gpsLocation!!
            "%.5f, %.5f (acc: %.0fm)".format(loc.latitude, loc.longitude, loc.accuracy)
        }
        else -> "No location yet"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Service status
            StatusRow(
                label = "Service",
                isActive = meshState.isServiceBound,
                activeText = "Service Connected",
                inactiveText = "Service Disconnected"
            )

            StatusRow(
                label = "GPS",
                isActive = isGpsReady,
                activeText = "Location available",
                inactiveText = "No location"
            )

            // Data cards
            LabeledCard(
                label = "Last packet type",
                value = meshState.lastPacketType
            )

            LabeledCard(
                label = "Last message",
                value = meshState.lastMessage
            )

            LabeledCard(
                label = "Position",
                value = locationText
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    gpsLocation?.let { loc ->
                        val cmd = "/distance ${loc.latitude} ${loc.longitude}"
                        meshViewModel.sendText(cmd)
                    }
                },
                enabled = canSendDistance,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test distance (current location)")
            }

            Button(
                onClick = {
                    meshViewModel.sendText("/signal")
                },
                enabled = meshState.isServiceBound,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test signal")
            }

            Button(
                onClick = {
                    if (allPermissionsGranted) {
                        gpsViewModel.requestFreshLocation { true }
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (allPermissionsGranted) "Refresh GPS" else "Grant GPS Permission")
            }
        }
    }
}

@Composable
fun StatusRow(
    label: String,
    isActive: Boolean,
    activeText: String,
    inactiveText: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = if (isActive)
                Color.Green
            else
                Color.Red,
            modifier = Modifier.size(12.dp)
        ) {}
        Spacer(Modifier.width(8.dp))
        Text(if (isActive) activeText else inactiveText)
    }
}

@Composable
fun LabeledCard(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value
            )
        }
    }
}