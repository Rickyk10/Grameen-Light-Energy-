package com.example.grameen_lightenergy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.grameen_lightenergy.data.model.Complaint
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {
    
    @Query("SELECT * FROM complaints ORDER BY timestamp DESC")
    fun getAllComplaints(): Flow<List<Complaint>>
    
    @Query("SELECT * FROM complaints WHERE status != 'FIXED' ORDER BY timestamp DESC")
    fun getActiveComplaints(): Flow<List<Complaint>>
    
    @Query("SELECT * FROM complaints WHERE poleId = :poleId ORDER BY timestamp DESC")
    fun getComplaintsByPoleId(poleId: String): Flow<List<Complaint>>

    @Query("SELECT * FROM complaints WHERE complaintId = :complaintId")
    suspend fun getComplaintById(complaintId: String): Complaint?
    
    @Query("SELECT COUNT(*) FROM complaints WHERE status != 'FIXED'")
    suspend fun getActiveComplaintCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: Complaint)
    
    @Update
    suspend fun updateComplaint(complaint: Complaint)
    
    @Query("DELETE FROM complaints")
    suspend fun deleteAllComplaints()
}
