package com.latefortrain.meshhelper.ui.startscreen.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.latefortrain.meshhelper.MainApplication

@Composable
fun ScannerDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val qrManager = (context.applicationContext as MainApplication).qrManager

    Dialog(onDismissRequest = onDismiss) {
        // Use a Surface or Card to give the dialog a shape
        Surface(
            modifier = Modifier.size(300.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black
        ) {
            AndroidView(
                factory = { ctx ->
                    androidx.camera.view.PreviewView(ctx).apply {
                        // Bind the camera to this view's lifecycle
                        qrManager.startScan(lifecycleOwner, this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}