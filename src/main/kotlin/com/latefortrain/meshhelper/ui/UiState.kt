package com.latefortrain.meshhelper.ui

data class UiState(
    val isConnected: Boolean = false,      // Mesh network status
    val isServiceBound: Boolean = false,   // App-to-App connection status
    val lastMessage: String = "Init...",
    val lastPacketType: String = "Init..."
)