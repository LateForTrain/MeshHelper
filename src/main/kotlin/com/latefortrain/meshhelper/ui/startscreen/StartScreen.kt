package com.latefortrain.meshhelper.ui.startscreen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.latefortrain.meshhelper.viewmodel.MeshViewModel

@Composable
fun StartScreen(meshViewModel: MeshViewModel) {
    val state by meshViewModel.uiState.collectAsState()

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
                isActive = state.isServiceBound,
                activeText = "Service Connected",
                inactiveText = "Service Disconnected"
            )

            // Data cards
            LabeledCard(
                label = "Last packet type",
                value = state.lastPacketType
            )

            LabeledCard(
                label = "Last message",
                value = state.lastMessage
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    //meshViewModel.requestDistance()
                    meshViewModel.sendText("/distance 57.7785 14.1697")
                },
                enabled = state.isServiceBound,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test distance")
            }

            Button(
                onClick = {
                    meshViewModel.sendText("/signal")
                },
                enabled = state.isServiceBound,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test signal")
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