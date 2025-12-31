package com.latefortrain.meshhelper.repository

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.latefortrain.meshhelper.ui.UiState
import com.latefortrain.meshhelper.util.getParcelableCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.meshtastic.core.model.DataPacket
import org.meshtastic.core.model.MeshUser
import org.meshtastic.core.model.MessageStatus
import org.meshtastic.core.model.MyNodeInfo
import org.meshtastic.core.model.NodeInfo
import org.meshtastic.core.model.Position
import org.meshtastic.core.service.IMeshService
import org.meshtastic.proto.Portnums

class MeshManager(private val context: Context) : DefaultLifecycleObserver {
    // Service Binding
    private var meshService: IMeshService? = null
    var isBound = false
        private set
    private var isBindingInProgress = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            meshService = IMeshService.Stub.asInterface(service)
            isBound = true
            isBindingInProgress = false // Reset here
            _uiState.update { it.copy(isServiceBound = true) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            meshService = null
            isBound = false
            _uiState.update { it.copy(isServiceBound = false) }
        }
    }

    // UI State exposed as a Flow
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    // Flows that the ViewModel can collect
    private val _nodeInfo = MutableStateFlow<NodeInfo?>(null)
    val nodeInfo: StateFlow<NodeInfo?> = _nodeInfo.asStateFlow()

    private val _dataPacket = MutableStateFlow<DataPacket?>(null)
    val dataPacket: StateFlow<DataPacket?> = _dataPacket.asStateFlow()

    private val _meshUser = MutableStateFlow<MeshUser?>(null)
    val meshUser: StateFlow<MeshUser?> = _meshUser.asStateFlow()

    private val _myNodeInfo = MutableStateFlow<MyNodeInfo?>(null)
    val myNodeInfo: StateFlow<MyNodeInfo?> = _myNodeInfo.asStateFlow()

    private val _meshPosition = MutableStateFlow<Position?>(null)
    val meshPosition: StateFlow<Position?> = _meshPosition.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // DEBUG: Log all keys in the intent to see what we actually got
            intent?.extras?.keySet()?.forEach { key ->
                _uiState.update { it.copy(lastPacketType = key) }
            }
            when (intent?.action) {
                "com.geeksville.mesh.NODE_CHANGE" -> {
                    val recvNodeInfo = intent.getParcelableCompat<NodeInfo>(
                        "com.geeksville.mesh.NodeInfo"
                    )
                    _nodeInfo.value = recvNodeInfo
                }
                "com.geeksville.mesh.RECEIVED.TEXT_MESSAGE_APP"-> {
                    val recvDataPacket = intent.getParcelableCompat<DataPacket>(
                        "com.geeksville.mesh.Payload"
                    )
                    _dataPacket.value = recvDataPacket
                    _uiState.update { it.copy(lastMessage = recvDataPacket?.text?: "Empty..") }
                }
                "com.geeksville.mesh.MESH_CONNECTED" -> _uiState.update { it.copy(isConnected = true) }
                "com.geeksville.mesh.MESH_DISCONNECTED" -> _uiState.update { it.copy(isConnected = false) }
            }
        }
    }

    fun connectToService(): Boolean {
        if (isBound || isBindingInProgress) return true

        val intent = Intent("com.geeksville.mesh.Service").apply {
            setClassName("com.geeksville.mesh", "com.geeksville.mesh.service.MeshService")
        }

        return try {
            val success = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (success) {
                isBindingInProgress = true // Flag that we are waiting for the callback
            }
            success
        } catch (e: Exception) {
            Log.e("MeshManager", "Binding exception", e)
            false
        }
    }

    fun disconnectFromService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        register()
        connectToService()
        Log.d("MeshManager", "Lifecycle: App started, registering receiver")
    }

    override fun onStop(owner: LifecycleOwner) {
        unregister()
        super.onStop(owner)
        Log.d("MeshManager", "Lifecycle: App stopped, unregistering receiver")
    }

    fun register() {
        val filter = IntentFilter().apply {
            addAction("com.geeksville.mesh.NODE_CHANGE")
            addAction("com.geeksville.mesh.RECEIVED.TEXT_MESSAGE_APP")
            addAction("com.geeksville.mesh.MESH_CONNECTED")
            addAction("com.geeksville.mesh.MESH_DISCONNECTED")
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
    }

    fun unregister() {
        context.unregisterReceiver(receiver)
    }

    fun sendTextMessage(text: String, destination: String = DataPacket.ID_BROADCAST) {
        val service = meshService
        if (service != null && isBound) {
            try {
                val packet = DataPacket(
                    to = destination,
                    bytes = text.toByteArray(),
                    dataType = Portnums.PortNum.TEXT_MESSAGE_APP_VALUE,
                    from = DataPacket.ID_LOCAL,
                    time = System.currentTimeMillis(),
                    id = 0,
                    status = MessageStatus.UNKNOWN,
                    hopLimit = 3,
                    channel = 0,
                    wantAck = true,
                )
                service.send(packet)
                Log.d("MeshManager", "Sent: $text")
            } catch (e: Exception) {
                Log.e("MeshManager", "Send failed", e)
            }
        } else {
            Log.w("MeshManager", "Send failed: Service not bound")
        }
    }
}