package com.example.grameen_lightenergy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "complaints")
data class Complaint(
    @PrimaryKey
    val complaintId: String = UUID.randomUUID().toString(),
    val poleId: String,
    val issueType: String,
    val status: ComplaintStatus,
    val timestamp: Long = System.currentTimeMillis()
)
