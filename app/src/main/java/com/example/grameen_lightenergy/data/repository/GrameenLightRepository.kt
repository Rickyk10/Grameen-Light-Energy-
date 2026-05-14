package com.example.grameen_lightenergy.data.repository

import com.example.grameen_lightenergy.data.dao.ComplaintDao
import com.example.grameen_lightenergy.data.dao.StreetlightPoleDao
import com.example.grameen_lightenergy.data.model.Complaint
import com.example.grameen_lightenergy.data.model.ComplaintStatus
import com.example.grameen_lightenergy.data.model.PoleStatus
import com.example.grameen_lightenergy.data.model.StreetlightPole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GrameenLightRepository(
    private val streetlightPoleDao: StreetlightPoleDao,
    private val complaintDao: ComplaintDao
) {
    
    fun getAllPoles(): Flow<List<StreetlightPole>> = streetlightPoleDao.getAllPoles()
    
    suspend fun getPoleById(poleId: String): StreetlightPole? = withContext(Dispatchers.IO) {
         streetlightPoleDao.getPoleById(poleId)
    }
    
    suspend fun updatePoleStatus(poleId: String, newStatus: PoleStatus) = withContext(Dispatchers.IO) {
        val pole = streetlightPoleDao.getPoleById(poleId)
        pole?.let {
            streetlightPoleDao.updatePole(it.copy(status = newStatus, lastUpdated = System.currentTimeMillis()))
        }
    }
    
    fun getAllComplaints(): Flow<List<Complaint>> = complaintDao.getAllComplaints()
    
    fun getActiveComplaints(): Flow<List<Complaint>> = complaintDao.getActiveComplaints()
    
    fun getComplaintsByPoleId(poleId: String): Flow<List<Complaint>> = complaintDao.getComplaintsByPoleId(poleId)
    
    suspend fun getActiveComplaintCount(): Int = withContext(Dispatchers.IO) {
        complaintDao.getActiveComplaintCount()
    }
    
    suspend fun submitComplaint(
        poleId: String,
        issueType: String,
        newPoleStatus: PoleStatus
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Create complaint
            val complaint = Complaint(
                poleId = poleId,
                issueType = issueType,
                status = ComplaintStatus.PENDING
            )
            complaintDao.insertComplaint(complaint)
            
            // Update pole status
            updatePoleStatus(poleId, newPoleStatus)
            
            Result.success(complaint.complaintId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateComplaintStatus(complaintId: String, newStatus: ComplaintStatus) = withContext(Dispatchers.IO) {
        val complaint = complaintDao.getComplaintById(complaintId)
        complaint?.let {
            complaintDao.updateComplaint(it.copy(status = newStatus))
        }
    }
}
