package com.example.androidapp3

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasureHuntScreen(
    waypoints: List<Waypoint>,
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current

    // App State
    var currentStopIndex by remember { mutableIntStateOf(0) }
    val activeWaypoint = waypoints[currentStopIndex]

    // Permission State
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Map Camera State
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(activeWaypoint.latLng, 15f)
    }

    LaunchedEffect(activeWaypoint) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(activeWaypoint.latLng, 15f)
    }

    // The actual GPS distance math
    fun verifyLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        location.latitude, location.longitude,
                        activeWaypoint.latLng.latitude, activeWaypoint.latLng.longitude,
                        results
                    )

                    val distanceInMeters = results[0]

                    if (distanceInMeters <= 50f) {
                        Toast.makeText(context, "Location Verified! Unlocking next clue...", Toast.LENGTH_SHORT).show()
                        if (currentStopIndex < waypoints.size - 1) {
                            currentStopIndex++
                        }
                    } else {
                        Toast.makeText(context, "You are ${distanceInMeters.toInt()} meters away. Get closer!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Waiting for GPS signal... Try opening Google Maps first.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Permission Launcher (Triggers ONLY when the button is clicked)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            hasLocationPermission = fineLocationGranted

            if (fineLocationGranted) {
                // If they just granted it, immediately verify so they don't have to click again!
                verifyLocation()
            } else {
                Toast.makeText(context, "Location permission is required to verify your spot!", Toast.LENGTH_LONG).show()
            }
        }
    )

    // User Interface
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("City Anniversary Hunt") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Map Section (Blue dot only turns on if permission is silently granted)
            GoogleMap(
                modifier = Modifier.weight(1f),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
            ) {
                Marker(
                    state = MarkerState(position = activeWaypoint.latLng),
                    title = activeWaypoint.name,
                    snippet = "Current Stop"
                )
            }

            // Interactive Clue Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Go to: ${activeWaypoint.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Next Clue: ${activeWaypoint.clueForNext}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentStopIndex < waypoints.size - 1) {
                        Button(onClick = {
                            // The Check: Do we have permission?
                            if (hasLocationPermission) {
                                verifyLocation() // Yes? Do the math.
                            } else {
                                // No? Ask for it now.
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }) {
                            Text("I'm Here! (Verify Location)")
                        }
                    } else {
                        Button(onClick = { /* TODO: Enter Draw */ }) {
                            Text("Enter Free Vacation Draw!")
                        }
                    }
                }
            }
        }
    }
}