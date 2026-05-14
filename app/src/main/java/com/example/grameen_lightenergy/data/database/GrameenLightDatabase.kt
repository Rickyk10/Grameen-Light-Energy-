package com.example.grameen_lightenergy.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.grameen_lightenergy.data.dao.ComplaintDao
import com.example.grameen_lightenergy.data.dao.StreetlightPoleDao
import com.example.grameen_lightenergy.data.model.Complaint
import com.example.grameen_lightenergy.data.model.PoleStatus
import com.example.grameen_lightenergy.data.model.StreetlightPole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [StreetlightPole::class, Complaint::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GrameenLightDatabase : RoomDatabase() {
    
    abstract fun streetlightPoleDao(): StreetlightPoleDao
    abstract fun complaintDao(): ComplaintDao
    
    companion object {
        @Volatile
        private var INSTANCE: GrameenLightDatabase? = null
        
        fun getDatabase(context: Context): GrameenLightDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GrameenLightDatabase::class.java,
                    "grameen_light_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                prePopulateDatabase(context)
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            CoroutineScope(Dispatchers.IO).launch {
                prePopulateDatabase(context)
            }
        }
        
        private suspend fun prePopulateDatabase(context: Context) {
            val database = getDatabase(context)
            val poleDao = database.streetlightPoleDao()
            
            // Insert 15 mock poles scattered in a realistic village layout (not a line)
            val mockPoles = listOf(
                StreetlightPole("POLE-001", PoleStatus.WORKING, 23.8100, 90.4100),
                StreetlightPole("POLE-002", PoleStatus.WORKING, 23.8200, 90.4150),
                StreetlightPole("POLE-003", PoleStatus.FUSED, 23.8150, 90.4200),
                StreetlightPole("POLE-004", PoleStatus.WORKING, 23.8050, 90.4180),
                StreetlightPole("POLE-005", PoleStatus.DAYTIME_ON, 23.8120, 90.4250),
                StreetlightPole("POLE-006", PoleStatus.WORKING, 23.8250, 90.4120),
                StreetlightPole("POLE-007", PoleStatus.WORKING, 23.8180, 90.4220),
                StreetlightPole("POLE-008", PoleStatus.FUSED, 23.8080, 90.4130),
                StreetlightPole("POLE-009", PoleStatus.WORKING, 23.8220, 90.4250),
                StreetlightPole("POLE-010", PoleStatus.WORKING, 23.8110, 90.4190),
                StreetlightPole("POLE-011", PoleStatus.DAYTIME_ON, 23.8190, 90.4110),
                StreetlightPole("POLE-012", PoleStatus.WORKING, 23.8060, 90.4210),
                StreetlightPole("POLE-013", PoleStatus.WORKING, 23.8240, 90.4160),
                StreetlightPole("POLE-014", PoleStatus.FUSED, 23.8130, 90.4120),
                StreetlightPole("POLE-015", PoleStatus.WORKING, 23.8210, 90.4190)
            )
            
            poleDao.insertPoles(mockPoles)
        }
    }
}
