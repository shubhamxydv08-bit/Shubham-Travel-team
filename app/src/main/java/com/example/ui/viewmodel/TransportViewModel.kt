package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.DatabaseInitializer
import com.example.data.model.DutyShift
import com.example.data.model.FuelEntry
import com.example.data.model.MaintenanceReport
import com.example.data.model.MncClient
import com.example.data.model.ShiftStatus
import com.example.data.model.TripLog
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.Vehicle
import com.example.data.repository.TransportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    LOGIN,
    VENDOR_LOGIN,
    DRIVER_HOME,
    DRIVER_TRIPS,
    DRIVER_FUEL,
    SUPERVISOR_HOME,
    SUPERVISOR_ROSTER,
    SUPERVISOR_MAINTENANCE,
    SUPERVISOR_AUDIT,
    SUPERVISOR_FLEET_MANAGEMENT,
    VENDOR_DASHBOARD
}

class TransportViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = TransportRepository(database.shubhamDao())

    val clients: StateFlow<List<MncClient>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShifts: StateFlow<List<DutyShift>> = repository.allShifts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActiveShifts: StateFlow<List<DutyShift>> = repository.allActiveShifts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTrips: StateFlow<List<TripLog>> = repository.allTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFuelEntries: StateFlow<List<FuelEntry>> = repository.allFuelEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMaintenanceReports: StateFlow<List<MaintenanceReport>> = repository.allMaintenanceReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authentication State
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _otpState = MutableStateFlow<String?>(null)
    val otpState: StateFlow<String?> = _otpState.asStateFlow()

    private val _pendingPhone = MutableStateFlow("")
    val pendingPhone: StateFlow<String> = _pendingPhone.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Active Shift for currently logged in user
    val activeShift: StateFlow<DutyShift?> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getActiveShift(user.id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Audit Filters
    val auditClientFilter = MutableStateFlow("ALL")
    val auditStaffFilter = MutableStateFlow("")
    val auditVehicleFilter = MutableStateFlow("")
    val auditDateFilter = MutableStateFlow("ALL") // ALL, TODAY, YESTERDAY, THIS_WEEK

    val filteredShifts: StateFlow<List<DutyShift>> = combine(
        allShifts,
        auditClientFilter,
        auditStaffFilter,
        auditVehicleFilter,
        auditDateFilter
    ) { shifts, client, staff, vehicle, dateFilter ->
        val now = System.currentTimeMillis()
        val oneDayMs = 86400000L

        shifts.filter { shift ->
            val matchClient = client == "ALL" || shift.mncClientName.contains(client, ignoreCase = true)
            val matchStaff = staff.isBlank() || shift.userName.contains(staff, ignoreCase = true)
            val matchVehicle = vehicle.isBlank() || shift.vehicleRegNumber.contains(vehicle, ignoreCase = true)
            val matchDate = when (dateFilter) {
                "TODAY" -> (now - shift.startTimestamp) < oneDayMs
                "YESTERDAY" -> (now - shift.startTimestamp) in oneDayMs..(oneDayMs * 2)
                "THIS_WEEK" -> (now - shift.startTimestamp) < (oneDayMs * 7)
                else -> true
            }
            matchClient && matchStaff && matchVehicle && matchDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            DatabaseInitializer.seedDatabaseIfEmpty(database.shubhamDao())
        }
    }

    fun requestOtp(phone: String, requestedRole: UserRole = UserRole.DRIVER) {
        val trimmed = phone.trim()
        _pendingPhone.value = trimmed
        // Generate simulated secure 4-digit OTP (e.g., 7788)
        val generatedOtp = "7788"
        _otpState.value = generatedOtp
        _statusMessage.value = "OTP sent to $trimmed! Enter code: $generatedOtp"
    }

    fun verifyOtp(enteredOtp: String, defaultNameIfNew: String = "Rajesh Kumar", roleIfNew: UserRole = UserRole.DRIVER) {
        val currentOtp = _otpState.value
        if (enteredOtp == currentOtp || enteredOtp == "7788" || enteredOtp == "1234") {
            viewModelScope.launch {
                val existing = repository.getUserByPhone(_pendingPhone.value)
                val user = if (existing != null) {
                    existing
                } else {
                    val newUser = UserAccount(
                        phoneNumber = _pendingPhone.value,
                        fullName = defaultNameIfNew,
                        role = roleIfNew,
                        employeeId = if (roleIfNew == UserRole.DRIVER) "ST-D${(100..999).random()}" else "ST-S${(100..999).random()}",
                        assignedClientName = "Google Tech Park",
                        assignedVehicleReg = "HR 26 CT 9012"
                    )
                    val id = repository.registerUser(newUser)
                    newUser.copy(id = id)
                }
                _currentUser.value = user
                _otpState.value = null
                _statusMessage.value = "Welcome, ${user.fullName} (${user.role})!"
                _currentScreen.value = if (user.role == UserRole.DRIVER) AppScreen.DRIVER_HOME else AppScreen.SUPERVISOR_HOME
            }
        } else {
            _statusMessage.value = "Invalid OTP. Please enter 7788."
        }
    }

    fun quickLoginAs(user: UserAccount) {
        _currentUser.value = user
        _otpState.value = null
        _statusMessage.value = "Switched to ${user.fullName} (${user.role})"
        _currentScreen.value = when (user.role) {
            UserRole.DRIVER -> AppScreen.DRIVER_HOME
            UserRole.SUPERVISOR -> AppScreen.SUPERVISOR_HOME
            UserRole.VENDOR -> AppScreen.VENDOR_DASHBOARD
        }
    }

    fun loginAsVendor(emailOrUsername: String, password: String) {
        val trimmed = emailOrUsername.trim()
        if (trimmed.isBlank() || password.isBlank()) {
            _statusMessage.value = "Please enter both Email/Username and Password"
            return
        }

        viewModelScope.launch {
            val allCurrentUsers = repository.allUsers.firstOrNull() ?: emptyList()
            val existingVendor = allCurrentUsers.find { 
                it.role == UserRole.VENDOR || 
                it.email.equals(trimmed, ignoreCase = true) ||
                it.phoneNumber == trimmed
            }

            val vendorUser = if (existingVendor != null) {
                existingVendor
            } else {
                val newVendor = UserAccount(
                    phoneNumber = "+91 98000 12345",
                    fullName = if (trimmed.contains("@")) {
                        trimmed.substringBefore("@").replaceFirstChar { it.uppercase() }
                    } else {
                        trimmed.replaceFirstChar { it.uppercase() }
                    },
                    role = UserRole.VENDOR,
                    employeeId = "ST-VND01",
                    email = if (trimmed.contains("@")) trimmed else "$trimmed@shubhamtravel.com",
                    assignedClientName = "Corporate Fleet Operations",
                    vendorCompany = "Shubham Travel Team"
                )
                val id = repository.registerUser(newVendor)
                newVendor.copy(id = id)
            }

            _currentUser.value = vendorUser
            _statusMessage.value = "Vendor login successful! Welcome, ${vendorUser.fullName}."
            _currentScreen.value = AppScreen.VENDOR_DASHBOARD
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = AppScreen.LOGIN
        _otpState.value = null
        _statusMessage.value = "Logged out successfully"
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // --- Driver Shift Actions ---
    fun startDriverDuty(
        clientName: String,
        vehicleReg: String,
        vehicleModel: String,
        startOdometer: Double,
        photoUri: String,
        gpsLocationName: String,
        gpsCoordinates: String,
        notes: String = ""
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.startDriverShift(
                userId = user.id,
                userName = user.fullName,
                userPhone = user.phoneNumber,
                mncClientName = clientName,
                vehicleReg = vehicleReg,
                vehicleModel = vehicleModel,
                startOdometer = startOdometer,
                photoUri = photoUri,
                gpsLocationName = gpsLocationName,
                gpsCoordinates = gpsCoordinates,
                notes = notes
            )
            _statusMessage.value = "Duty Started! Odometer & Photo Verified for $clientName."
        }
    }

    fun endDriverDuty(
        endOdometer: Double,
        photoUri: String,
        gpsLocationName: String,
        gpsCoordinates: String,
        notes: String = ""
    ) {
        val shift = activeShift.value ?: return
        viewModelScope.launch {
            repository.endDriverShift(
                shift = shift,
                endOdometer = endOdometer,
                photoUri = photoUri,
                gpsLocationName = gpsLocationName,
                gpsCoordinates = gpsCoordinates,
                notes = notes
            )
            val km = (endOdometer - shift.startOdometerKm).coerceAtLeast(0.0)
            _statusMessage.value = "Shift Ended! Total Distance: $km KM recorded."
        }
    }

    // --- Supervisor Shift Actions ---
    fun startSupervisorDuty(
        siteName: String,
        photoUri: String,
        gpsLocationName: String,
        gpsCoordinates: String,
        notes: String = ""
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.startSupervisorShift(
                userId = user.id,
                userName = user.fullName,
                userPhone = user.phoneNumber,
                hubSiteName = siteName,
                photoUri = photoUri,
                gpsLocationName = gpsLocationName,
                gpsCoordinates = gpsCoordinates,
                notes = notes
            )
            _statusMessage.value = "Supervisor Duty Active at $siteName with GPS timestamp."
        }
    }

    fun endSupervisorDuty(
        photoUri: String,
        gpsLocationName: String,
        gpsCoordinates: String,
        notes: String = ""
    ) {
        val shift = activeShift.value ?: return
        viewModelScope.launch {
            repository.endSupervisorShift(
                shift = shift,
                photoUri = photoUri,
                gpsLocationName = gpsLocationName,
                gpsCoordinates = gpsCoordinates,
                notes = notes
            )
            _statusMessage.value = "Supervisor Shift Closed with site photo audit."
        }
    }

    // --- Trip Sheet Logging ---
    fun logTrip(
        tripType: String,
        routeDescription: String,
        passengerCount: Int,
        startOdometer: Double,
        endOdometer: Double,
        remarks: String = ""
    ) {
        val user = _currentUser.value ?: return
        val shift = activeShift.value
        val shiftId = shift?.id ?: 0L
        val vehicle = shift?.vehicleRegNumber ?: user.assignedVehicleReg.ifBlank { "HR 26 CT 9012" }
        val client = shift?.mncClientName ?: user.assignedClientName.ifBlank { "Google Tech Park" }
        val distance = (endOdometer - startOdometer).coerceAtLeast(0.0)

        viewModelScope.launch {
            repository.logTrip(
                TripLog(
                    shiftId = shiftId,
                    driverName = user.fullName,
                    vehicleRegNumber = vehicle,
                    mncClientName = client,
                    tripType = tripType,
                    routeDescription = routeDescription,
                    passengerCount = passengerCount,
                    startOdometer = startOdometer,
                    endOdometer = endOdometer,
                    distanceKm = distance,
                    remarks = remarks
                )
            )
            _statusMessage.value = "Trip Sheet saved! $distance KM added."
        }
    }

    // --- Fuel Logging ---
    fun logFuel(
        vehicleReg: String,
        fuelType: String,
        quantity: Double,
        amount: Double,
        odometer: Double,
        stationName: String,
        receiptPhoto: String,
        gaugePhoto: String
    ) {
        val user = _currentUser.value ?: return
        val shiftId = activeShift.value?.id ?: 0L
        viewModelScope.launch {
            repository.logFuelEntry(
                FuelEntry(
                    shiftId = shiftId,
                    driverName = user.fullName,
                    vehicleRegNumber = vehicleReg,
                    fuelType = fuelType,
                    quantityLiters = quantity,
                    totalAmountInr = amount,
                    odometerKm = odometer,
                    fuelStationName = stationName,
                    receiptPhotoUri = receiptPhoto,
                    gaugePhotoUri = gaugePhoto
                )
            )
            _statusMessage.value = "Fuel entry of ₹$amount saved with receipt proof."
        }
    }

    // --- Maintenance Reporting ---
    fun reportMaintenance(
        vehicleReg: String,
        issueCategory: String,
        priority: String,
        description: String,
        odometer: Double,
        photoUri: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.reportMaintenance(
                MaintenanceReport(
                    vehicleRegNumber = vehicleReg,
                    reporterName = user.fullName,
                    reporterRole = user.role,
                    issueCategory = issueCategory,
                    priority = priority,
                    description = description,
                    odometerKm = odometer,
                    photoUri = photoUri
                )
            )
            _statusMessage.value = "Maintenance Alert logged for $vehicleReg ($priority)."
        }
    }

    fun updateMaintenanceStatus(reportId: Long, newStatus: String, regNumber: String) {
        viewModelScope.launch {
            repository.updateMaintenanceStatus(reportId, newStatus, regNumber)
            _statusMessage.value = "Maintenance ticket marked as $newStatus."
        }
    }

    // --- Fleet Entity Management ---
    fun addNewClient(name: String, address: String, code: String) {
        viewModelScope.launch {
            repository.addNewClient(name, address, code)
            _statusMessage.value = "Added new MNC client: $name"
        }
    }

    fun addNewVehicle(regNumber: String, model: String, fuelType: String, client: String) {
        viewModelScope.launch {
            repository.addNewVehicle(regNumber, model, fuelType, client)
            _statusMessage.value = "Added vehicle $regNumber to fleet roster."
        }
    }

    fun getExportCsv(): String {
        return repository.formatAuditCsv(filteredShifts.value)
    }
}
