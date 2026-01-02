package com.latefortrain.meshhelper.ui.startscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.latefortrain.meshhelper.ui.UiState
import com.latefortrain.meshhelper.ui.startscreen.components.LabeledCard
import com.latefortrain.meshhelper.ui.startscreen.components.StatusRow

@Composable
fun StartScreenContent(
    meshState: UiState,
    locationText: String,
    isGpsReady: Boolean,
    allPermissionsGranted: Boolean,
    onTestDistance: () -> Unit,
    onTestSignal: () -> Unit,
    onScanQR: () -> Unit,
    onRefreshGPS: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusRow("Service", meshState.isServiceBound, "Service Connected", "Service Disconnected")
            StatusRow("GPS", isGpsReady, "Location available", "No location")

            LabeledCard("Last packet type", meshState.lastPacketType)
            LabeledCard("Last message", meshState.lastMessage)
            LabeledCard("Position", locationText)

            Spacer(Modifier.height(8.dp))

            Button(onClick = onTestDistance, enabled = meshState.isServiceBound && isGpsReady, modifier = Modifier.fillMaxWidth()) {
                Text("Test distance (current location)")
            }
            Button(onClick = onTestSignal, enabled = meshState.isServiceBound, modifier = Modifier.fillMaxWidth()) {
                Text("Test signal")
            }
            Button(onClick = onScanQR, modifier = Modifier.fillMaxWidth()) {
                Text("Scan QR & Send via Mesh")
            }
            Button(onClick = onRefreshGPS, modifier = Modifier.fillMaxWidth()) {
                Text(if (allPermissionsGranted) "Refresh GPS" else "Grant GPS Permission")
            }
        }
    }
}