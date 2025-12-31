package com.latefortrain.meshhelper

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.latefortrain.meshhelper.repository.GPSManager
import com.latefortrain.meshhelper.ui.startscreen.StartScreen
import com.latefortrain.meshhelper.viewmodel.GPSViewModel
import com.latefortrain.meshhelper.viewmodel.MeshViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val meshManager = (application as MainApplication).meshManager
        val gpsManager = (application as MainApplication).gpsManager

        setContent {
            val meshViewModel: MeshViewModel = viewModel {
                MeshViewModel(meshManager)
            }
            val gpsViewModel: GPSViewModel = viewModel {
                GPSViewModel(gpsManager)
            }
            StartScreen(meshViewModel,gpsViewModel)
        }
    }
}