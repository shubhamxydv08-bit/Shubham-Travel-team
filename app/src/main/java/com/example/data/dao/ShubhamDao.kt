package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DutyShift
import com.example.data.model.FuelEntry
import com.example.data.model.MaintenanceReport
import com.example.data.model.MncClient
import com.example.data.model.ShiftStatus
import com.example.data.model.TripLog
import com.example.data.model.UserAccount
import com.example.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface ShubhamDao {

    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserAccount?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserAccount>)

    // --- MNC Clients ---
    @Query("SELECT * FROM mnc_clients ORDER BY name ASC")
    fun getAllMncClients(): Flow<List<MncClient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMncClient(client: MncClient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMncClients(clients: List<MncClient>)

    // --- Vehicles ---
    @Query("SELECT * FROM vehicles ORDER BY registrationNumber ASC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE registrationNumber = :reg LIMIT 1")
    suspend fun getVehicleByReg(reg: String): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<Vehicle>)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Query("UPDATE vehicles SET currentOdometerKm = :odometer WHERE registrationNumber = :reg")
    suspend fun updateVehicleOdometer(reg: String, odometer: Double)

    @Query("UPDATE vehicles SET status = :status WHERE registrationNumber = :reg")
    suspend fun updateVehicleStatus(reg: String, status: String)

    // --- Duty Shifts ---
    @Query("SELECT * FROM duty_shifts ORDER BY startTimestamp DESC")
    fun getAllShifts(): Flow<List<DutyShift>>

    @Query("SELECT * FROM duty_shifts WHERE userId = :userId AND status = 'ACTIVE' LIMIT 1")
    fun getActiveShiftForUser(userId: Long): Flow<DutyShift?>

    @Query("SELECT * FROM duty_shifts WHERE status = 'ACTIVE' ORDER BY startTimestamp DESC")
    fun getAllActiveShifts(): Flow<List<DutyShift>>

    @Query("SELECT * FROM duty_shifts WHERE id = :shiftId LIMIT 1")
    suspend fun getShiftById(shiftId: Long): DutyShift?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: DutyShift): Long

    @Update
    suspend fun updateShift(shift: DutyShift)

    // --- Trip Logs ---
    @Query("SELECT * FROM trip_logs ORDER BY timestamp DESC")
    fun getAllTrips(): Flow<List<TripLog>>

    @Query("SELECT * FROM trip_logs WHERE shiftId = :shiftId ORDER BY timestamp DESC")
    fun getTripsForShift(shiftId: Long): Flow<List<TripLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripLog): Long

    // --- Fuel Entries ---
    @Query("SELECT * FROM fuel_entries ORDER BY timestamp DESC")
    fun getAllFuelEntries(): Flow<List<FuelEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelEntry(entry: FuelEntry): Long

    // --- Maintenance Reports ---
    @Query("SELECT * FROM maintenance_reports ORDER BY timestamp DESC")
    fun getAllMaintenanceReports(): Flow<List<MaintenanceReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceReport(report: MaintenanceReport): Long

    @Query("UPDATE maintenance_reports SET status = :status WHERE id = :reportId")
    suspend fun updateMaintenanceStatus(reportId: Long, status: String)
}
