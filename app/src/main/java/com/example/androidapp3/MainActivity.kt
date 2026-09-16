package com.example.androidapp3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.androidapp3.ui.theme.AndroidApp3Theme
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the location client
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            AndroidApp3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass both the data and the location client to the UI
                    TreasureHuntScreen(
                        waypoints = TreasureHuntData.locations,
                        fusedLocationClient = fusedLocationClient
                    )
                }
            }
        }
    }
}