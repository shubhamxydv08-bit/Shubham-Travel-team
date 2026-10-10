package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    DRIVER,
    SUPERVISOR,
    VENDOR
}

enum class ShiftStatus {
    ACTIVE,
    COMPLETED
}

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val fullName: String,
    val role: UserRole,
    val employeeId: String,
    val assignedClientName: String = "",
    val assignedVehicleReg: String = "",
    val profilePhotoUri: String = "",
    val email: String = "",
    val vendorCompany: String = "Shubham Travel Team"
)

@Entity(tableName = "mnc_clients")
data class MncClient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val campusAddress: String,
    val activeCabQuota: Int = 10,
    val contactPerson: String = "",
    val contactPhone: String = ""
)

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val registrationNumber: String,
    val model: String,
    val fuelType: String, // Diesel, Petrol, CNG, EV
    val seatingCapacity: Int = 4,
    val currentOdometerKm: Double = 0.0,
    val assignedDriverName: String = "",
    val assignedMncClient: String = "",
    val status: String = "ACTIVE" // ACTIVE, IN_SERVICE, GROUNDED
)

@Entity(tableName = "duty_shifts")
data class DutyShift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val userPhone: String,
    val userRole: UserRole,
    val mncClientName: String,
    val vehicleRegNumber: String,
    val vehicleModel: String,
    val startTimestamp: Long = System.currentTimeMillis(),
    val endTimestamp: Long? = null,
    val startOdometerKm: Double = 0.0,
    val endOdometerKm: Double? = null,
    val totalKmTraveled: Double = 0.0,
    val startPhotoUri: String = "",
    val endPhotoUri: String = "",
    val startGpsLocationName: String = "",
    val startGpsCoordinates: String = "",
    val endGpsLocationName: String = "",
    val endGpsCoordinates: String = "",
    val status: ShiftStatus = ShiftStatus.ACTIVE,
    val notes: String = ""
)

@Entity(tableName = "trip_logs")
data class TripLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shiftId: Long,
    val driverName: String,
    val vehicleRegNumber: String,
    val mncClientName: String,
    val tripType: String, // Morning Pickup, Evening Drop, Airport Transfer, Inter-Campus
    val routeDescription: String,
    val passengerCount: Int = 1,
    val startOdometer: Double = 0.0,
    val endOdometer: Double = 0.0,
    val distanceKm: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val remarks: String = ""
)

@Entity(tableName = "fuel_entries")
data class FuelEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shiftId: Long = 0,
    val driverName: String,
    val vehicleRegNumber: String,
    val fuelType: String, // Diesel, Petrol, CNG, EV
    val quantityLiters: Double,
    val totalAmountInr: Double,
    val odometerKm: Double,
    val receiptPhotoUri: String = "",
    val gaugePhotoUri: String = "",
    val fuelStationName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "maintenance_reports")
data class MaintenanceReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleRegNumber: String,
    val reporterName: String,
    val reporterRole: UserRole,
    val issueCategory: String, // Tyre Puncture, Brake Wear, AC Malfunction, Oil Service, Body Scratch
    val priority: String, // HIGH (Ground Vehicle), MEDIUM (24h), LOW (Routine)
    val description: String,
    val odometerKm: Double = 0.0,
    val photoUri: String = "",
    val status: String = "REPORTED", // REPORTED, IN_REPAIR, RESOLVED
    val timestamp: Long = System.currentTimeMillis()
)
