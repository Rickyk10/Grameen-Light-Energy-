package com.example.grameen_lightenergy.data.database

import androidx.room.TypeConverter
import com.example.grameen_lightenergy.data.model.ComplaintStatus
import com.example.grameen_lightenergy.data.model.PoleStatus

class Converters {
    
    @TypeConverter
    fun fromPoleStatus(status: PoleStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toPoleStatus(status: String): PoleStatus {
        return PoleStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromComplaintStatus(status: ComplaintStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toComplaintStatus(status: String): ComplaintStatus {
        return ComplaintStatus.valueOf(status)
    }
}
