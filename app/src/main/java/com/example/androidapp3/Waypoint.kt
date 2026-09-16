package com.example.androidapp3

import com.google.android.gms.maps.model.LatLng

data class Waypoint(
    val id: Int,
    val name: String,
    val latLng: LatLng,
    val clueForNext: String
)