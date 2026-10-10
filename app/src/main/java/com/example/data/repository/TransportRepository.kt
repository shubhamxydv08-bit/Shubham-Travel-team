package com.example.data.repository

import com.example.data.dao.ShubhamDao
import com.example.data.model.DutyShift
import com.example.data.model.FuelEntry
import com.example.data.model.MaintenanceReport
import com.example.data.model.MncClient
import com.example.data.model.ShiftStatus
import com.example.data.model.TripLog
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.Vehicle
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransportRepository(private val dao: ShubhamDao) {

    val allClients: Flow<List<MncClient>> = dao.getAllMncClients()
    val allVehicles: Flow<List<Vehicle>> = dao.getAllVehicles()
    val allUsers: Flow<List<UserAccount>> = dao.getAllUsers()
    val allShifts: Flow<List<DutyShift>> = dao.getAllShifts()
    val allTrips: Flow<List<TripLog>> = dao.getAllTrips()
    val allFuelEntries: Flow<List<FuelEntry>> = dao.getAllFuelEntries()
    val allMaintenanceReports: Flow<List<MaintenanceReport>> = dao.getAllMaintenanceReports()
    val allActiveShifts: Flow<List<DutyShift>> = dao.getAllActiveShifts()

    fun getActiveShift(userId: Long): Flow<DutyShift?> = dao.getActiveShiftForUser(userId)

    fun getTripsForShift(shiftId: Long): Flow<List<TripLog>> = dao.getTripsForShift(shiftId)

    suspend fun getUserByPhone(phone: String): UserAccount? = dao.getUserByPhone(phone)

    suspend fun getUserById(id: Long): UserAccount? = dao.getUserById(id)

    suspend fun registerUser(user: UserAccount): Long = dao.insertUser(user)

    suspend fun startDriverShift(
        userId: Long,
        userName: String,
        userPhone: String,
        mncClientName: String,
        vehicleReg: String,
        vehicleModel: String,
        startOdometer: Double,
        photoUri: String,
        gpsLocationName: String,
        gpsCoordinates: String,
        notes: String = ""
    ): Long {
        val shift = DutyShift(
            userId = userId,
            userName = userName,
            userPhone = userPhone,
            userRole = UserRole.DRIVER,
            mncClientName = mncClientName,
            vehicleRegNumber = vehicleReg,
            vehicleModel = vehicleModel,
            startTimestamp = System.currentTimeMillis(),
            startOdometerKm = startOdometer,
            startPhotoUri = photoUri,
            startGpsLocationName = gpsLocationName,
            startGpsCoordinates = gpsCoordinates,
            status = ShiftStatus.ACTIVE,
            notes = notes
        )
        val id = dao.insertShift(shift)
        dao.updateVehicleOdometer(vehicleReg, startOdometer)
        return id
    }

    suspend fun endDriverShift(
        shift: DutyShift,
        endOdometer: Double,
        photoUri: String,
        gpsLocationName: String,
        gpsCoordinates: String,
        notes: String = ""
    ) {
        val totalKm = (endOdometer - shift.startOdometerKm).coerceAtLeast(0.0)
        val updatedShift = shift.copy(
            endTimestamp = System.currentTimeMillis(),
            endOdometerKm = endOdometer,
            totalKmTraveled = totalKm,
            endPhotoUri = photoUri,
            endGpsLocationName = gpsLocationName,
            endGpsCoordinates = gpsCoordinates,
            status = ShiftStatus.COMPLETED,
            notes = if (notes.isNotBlank()) "${shift.notes} | $notes" else shift.notes
        )
        dao.updateShift(updatedShift)
        dao.updateVehicleOdometer(shift.vehicleRegNumber, endOdometer)
    }

    suspend fun startSupervisorShift(
        userId: Long,
        userName: String,
        userPhone: String,
        hubSiteName: String,
        photoUri: String,
        gpsLocationName: String,
        gpsCoordinates: String,
        notes: String = ""
    ): Long {
        val shift = DutyShift(
            userId = userId,
            userName = userName,
            userPhone = userPhone,
            userRole = UserRole.SUPERVISOR,
            mncClientName = hubSiteName,
            vehicleRegNumber = "HUB-SUPERVISOR",
            vehicleModel = "On-Site Management",
            startTimestamp = System.currentTimeMillis(),
            startOdometerKm = 0.0,
            startPhotoUri = photoUri,
            startGpsLocationName = gpsLocationName,
            startGpsCoordinates = gpsCoordinates,
            status = ShiftStatus.ACTIVE,
            notes = notes
        )
        return dao.insertShift(shift)
    }

    suspend fun endSupervisorShift(
        shift: DutyShift,
        photoUri: String,
        gpsLocationName: String,
        gpsCoordinates: String,
        notes: String = ""
    ) {
        val updatedShift = shift.copy(
            endTimestamp = System.currentTimeMillis(),
            endPhotoUri = photoUri,
            endGpsLocationName = gpsLocationName,
            endGpsCoordinates = gpsCoordinates,
            status = ShiftStatus.COMPLETED,
            notes = if (notes.isNotBlank()) "${shift.notes} | Closing: $notes" else shift.notes
        )
        dao.updateShift(updatedShift)
    }

    suspend fun logTrip(trip: TripLog): Long {
        val id = dao.insertTrip(trip)
        if (trip.endOdometer > 0) {
            dao.updateVehicleOdometer(trip.vehicleRegNumber, trip.endOdometer)
        }
        return id
    }

    suspend fun logFuelEntry(entry: FuelEntry): Long {
        val id = dao.insertFuelEntry(entry)
        if (entry.odometerKm > 0) {
            dao.updateVehicleOdometer(entry.vehicleRegNumber, entry.odometerKm)
        }
        return id
    }

    suspend fun reportMaintenance(report: MaintenanceReport): Long {
        val id = dao.insertMaintenanceReport(report)
        if (report.priority.contains("HIGH")) {
            dao.updateVehicleStatus(report.vehicleRegNumber, "GROUNDED")
        } else {
            dao.updateVehicleStatus(report.vehicleRegNumber, "IN_SERVICE")
        }
        return id
    }

    suspend fun updateMaintenanceStatus(reportId: Long, newStatus: String, reg: String) {
        dao.updateMaintenanceStatus(reportId, newStatus)
        if (newStatus == "RESOLVED") {
            dao.updateVehicleStatus(reg, "ACTIVE")
        }
    }

    suspend fun addNewClient(name: String, campusAddress: String, code: String): Long {
        return dao.insertMncClient(
            MncClient(
                name = name,
                code = code,
                campusAddress = campusAddress,
                activeCabQuota = 10
            )
        )
    }

    suspend fun addNewVehicle(regNumber: String, model: String, fuelType: String, client: String): Long {
        return dao.insertVehicle(
            Vehicle(
                registrationNumber = regNumber,
                model = model,
                fuelType = fuelType,
                assignedMncClient = client,
                status = "ACTIVE"
            )
        )
    }

    fun formatAuditCsv(shifts: List<DutyShift>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.append("Shift ID,Role,Staff Name,Phone,MNC Client,Vehicle Reg,Model,Duty Start,Start Odometer,Start GPS Site,Start Coordinates,Photo Verified,Duty End,End Odometer,End GPS Site,End Coordinates,Total KM,Status,Notes\n")
        shifts.forEach { s ->
            val startStr = sdf.format(Date(s.startTimestamp))
            val endStr = s.endTimestamp?.let { sdf.format(Date(it)) } ?: "ON-DUTY (Active)"
            val photoVerified = if (s.startPhotoUri.isNotBlank()) "YES" else "NO"
            sb.append("${s.id},\"${s.userRole}\",\"${s.userName}\",\"${s.userPhone}\",\"${s.mncClientName}\",\"${s.vehicleRegNumber}\",\"${s.vehicleModel}\",\"$startStr\",${s.startOdometerKm},\"${s.startGpsLocationName}\",\"${s.startGpsCoordinates}\",\"$photoVerified\",\"$endStr\",${s.endOdometerKm ?: 0.0},\"${s.endGpsLocationName}\",\"${s.endGpsCoordinates}\",${s.totalKmTraveled},\"${s.status}\",\"${s.notes}\"\n")
        }
        return sb.toString()
    }
}
