package com.example.grameen_lightenergy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.grameen_lightenergy.data.model.StreetlightPole
import kotlinx.coroutines.flow.Flow

@Dao
interface StreetlightPoleDao {
    
    @Query("SELECT * FROM streetlight_poles")
    fun getAllPoles(): Flow<List<StreetlightPole>>
    
    @Query("SELECT * FROM streetlight_poles WHERE id = :poleId")
    suspend fun getPoleById(poleId: String): StreetlightPole?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPole(pole: StreetlightPole)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoles(poles: List<StreetlightPole>)
    
    @Update
    suspend fun updatePole(pole: StreetlightPole)
    
    @Query("DELETE FROM streetlight_poles")
    suspend fun deleteAllPoles()
}
