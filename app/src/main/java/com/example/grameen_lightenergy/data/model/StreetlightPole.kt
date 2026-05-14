package com.example.grameen_lightenergy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streetlight_poles")
data class StreetlightPole(
    @PrimaryKey
    val id: String,
    val status: PoleStatus,
    val lat: Double,
    val lng: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
