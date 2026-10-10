package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ShubhamDao
import com.example.data.model.DutyShift
import com.example.data.model.FuelEntry
import com.example.data.model.MaintenanceReport
import com.example.data.model.MncClient
import com.example.data.model.TripLog
import com.example.data.model.UserAccount
import com.example.data.model.Vehicle

@Database(
    entities = [
        UserAccount::class,
        MncClient::class,
        Vehicle::class,
        DutyShift::class,
        TripLog::class,
        FuelEntry::class,
        MaintenanceReport::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shubhamDao(): ShubhamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shubham_travel_fleet.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
