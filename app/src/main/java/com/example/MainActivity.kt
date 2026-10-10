package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRole
import com.example.ui.components.TopHeaderBar
import com.example.ui.components.UserSwitcherDialog
import com.example.ui.viewmodel.AppScreen
import com.example.ui.screens.AuditDashboardScreen
import com.example.ui.screens.DriverDashboardScreen
import com.example.ui.screens.DutyActionDialog
import com.example.ui.screens.DutyActionMode
import com.example.ui.screens.FleetManagementScreen
import com.example.ui.screens.FuelManagementScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MaintenanceScreen
import com.example.ui.screens.SupervisorDashboardScreen
import com.example.ui.screens.TripSheetScreen
import com.example.ui.screens.VendorDashboardScreen
import com.example.ui.screens.VendorLoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TransportViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TransportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: TransportViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeShift by viewModel.activeShift.collectAsStateWithLifecycle()
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val allShifts by viewModel.allShifts.collectAsStateWithLifecycle()
    val allActiveShifts by viewModel.allActiveShifts.collectAsStateWithLifecycle()
    val allTrips by viewModel.allTrips.collectAsStateWithLifecycle()
    val allFuelEntries by viewModel.allFuelEntries.collectAsStateWithLifecycle()
    val allMaintenanceReports by viewModel.allMaintenanceReports.collectAsStateWithLifecycle()
    val filteredShifts by viewModel.filteredShifts.collectAsStateWithLifecycle()

    val otpState by viewModel.otpState.collectAsStateWithLifecycle()
    val pendingPhone by viewModel.pendingPhone.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val auditClientFilter by viewModel.auditClientFilter.collectAsStateWithLifecycle()
    val auditStaffFilter by viewModel.auditStaffFilter.collectAsStateWithLifecycle()
    val auditVehicleFilter by viewModel.auditVehicleFilter.collectAsStateWithLifecycle()
    val auditDateFilter by viewModel.auditDateFilter.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var activeDutyActionMode by remember { mutableStateOf<DutyActionMode?>(null) }
    var showUserSwitcher by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F172A),
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header bar visible whenever user is logged in
            if (currentUser != null && currentScreen != AppScreen.LOGIN && currentScreen != AppScreen.VENDOR_LOGIN) {
                TopHeaderBar(
                    user = currentUser,
                    activeShift = activeShift,
                    onSwitchUser = { showUserSwitcher = true },
                    onLogout = { viewModel.logout() }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    AppScreen.LOGIN -> {
                        LoginScreen(
                            preloadedUsers = users,
                            pendingPhone = pendingPhone,
                            activeOtp = otpState,
                            onRequestOtp = { phone, role -> viewModel.requestOtp(phone, role) },
                            onVerifyOtp = { otp, name, role -> viewModel.verifyOtp(otp, name, role) },
                            onQuickLogin = { user -> viewModel.quickLoginAs(user) },
                            onNavigateToVendorLogin = { viewModel.navigateTo(AppScreen.VENDOR_LOGIN) }
                        )
                    }

                    AppScreen.VENDOR_LOGIN -> {
                        VendorLoginScreen(
                            onLoginVendor = { email, pass -> viewModel.loginAsVendor(email, pass) },
                            onNavigateToDriverSupervisorLogin = { viewModel.navigateTo(AppScreen.LOGIN) }
                        )
                    }

                    AppScreen.DRIVER_HOME -> {
                        val activeUser = currentUser ?: return@Box
                        DriverDashboardScreen(
                            user = activeUser,
                            activeShift = activeShift,
                            todayTrips = allTrips.filter { it.driverName == activeUser.fullName },
                            onOpenDutyDialog = { mode -> activeDutyActionMode = mode },
                            onNavigateToTrips = { viewModel.navigateTo(AppScreen.DRIVER_TRIPS) },
                            onNavigateToFuel = { viewModel.navigateTo(AppScreen.DRIVER_FUEL) },
                            onNavigateToMaintenance = { viewModel.navigateTo(AppScreen.SUPERVISOR_MAINTENANCE) }
                        )
                    }

                    AppScreen.DRIVER_TRIPS -> {
                        val activeUser = currentUser ?: return@Box
                        TripSheetScreen(
                            user = activeUser,
                            activeShift = activeShift,
                            allTrips = allTrips,
                            onBack = { viewModel.navigateTo(AppScreen.DRIVER_HOME) },
                            onSaveTrip = { type, route, pax, sOdo, eOdo, remarks ->
                                viewModel.logTrip(type, route, pax, sOdo, eOdo, remarks)
                            }
                        )
                    }

                    AppScreen.DRIVER_FUEL -> {
                        val activeUser = currentUser ?: return@Box
                        FuelManagementScreen(
                            user = activeUser,
                            activeShift = activeShift,
                            availableVehicles = vehicles,
                            allFuelEntries = allFuelEntries,
                            onBack = {
                                viewModel.navigateTo(
                                    when (activeUser.role) {
                                        UserRole.DRIVER -> AppScreen.DRIVER_HOME
                                        UserRole.SUPERVISOR -> AppScreen.SUPERVISOR_HOME
                                        UserRole.VENDOR -> AppScreen.VENDOR_DASHBOARD
                                    }
                                )
                            },
                            onSaveFuel = { reg, fuel, qty, amt, odo, stn, rPhoto, gPhoto ->
                                viewModel.logFuel(reg, fuel, qty, amt, odo, stn, rPhoto, gPhoto)
                            }
                        )
                    }

                    AppScreen.SUPERVISOR_HOME -> {
                        val activeUser = currentUser ?: return@Box
                        SupervisorDashboardScreen(
                            user = activeUser,
                            activeShift = activeShift,
                            activeShiftsList = allActiveShifts,
                            allShiftsList = allShifts,
                            vehiclesList = vehicles,
                            clientsList = clients,
                            maintenanceList = allMaintenanceReports,
                            onOpenDutyDialog = { mode -> activeDutyActionMode = mode },
                            onNavigateToAudit = { viewModel.navigateTo(AppScreen.SUPERVISOR_AUDIT) },
                            onNavigateToMaintenance = { viewModel.navigateTo(AppScreen.SUPERVISOR_MAINTENANCE) },
                            onNavigateToFleet = { viewModel.navigateTo(AppScreen.SUPERVISOR_FLEET_MANAGEMENT) }
                        )
                    }

                    AppScreen.SUPERVISOR_AUDIT, AppScreen.SUPERVISOR_ROSTER -> {
                        val activeUser = currentUser ?: return@Box
                        AuditDashboardScreen(
                            shifts = filteredShifts,
                            clients = clients,
                            selectedClientFilter = auditClientFilter,
                            staffFilter = auditStaffFilter,
                            vehicleFilter = auditVehicleFilter,
                            dateFilter = auditDateFilter,
                            onClientFilterChanged = { viewModel.auditClientFilter.value = it },
                            onStaffFilterChanged = { viewModel.auditStaffFilter.value = it },
                            onVehicleFilterChanged = { viewModel.auditVehicleFilter.value = it },
                            onDateFilterChanged = { viewModel.auditDateFilter.value = it },
                            onBack = {
                                viewModel.navigateTo(
                                    when (activeUser.role) {
                                        UserRole.DRIVER -> AppScreen.DRIVER_HOME
                                        UserRole.SUPERVISOR -> AppScreen.SUPERVISOR_HOME
                                        UserRole.VENDOR -> AppScreen.VENDOR_DASHBOARD
                                    }
                                )
                            },
                            onGenerateCsv = { viewModel.getExportCsv() }
                        )
                    }

                    AppScreen.SUPERVISOR_MAINTENANCE -> {
                        val activeUser = currentUser ?: return@Box
                        MaintenanceScreen(
                            user = activeUser,
                            vehicles = vehicles,
                            allReports = allMaintenanceReports,
                            onBack = {
                                viewModel.navigateTo(
                                    when (activeUser.role) {
                                        UserRole.DRIVER -> AppScreen.DRIVER_HOME
                                        UserRole.SUPERVISOR -> AppScreen.SUPERVISOR_HOME
                                        UserRole.VENDOR -> AppScreen.VENDOR_DASHBOARD
                                    }
                                )
                            },
                            onReportMaintenance = { reg, cat, prio, desc, odo, photo ->
                                viewModel.reportMaintenance(reg, cat, prio, desc, odo, photo)
                            },
                            onResolveReport = { id, reg ->
                                viewModel.updateMaintenanceStatus(id, "RESOLVED", reg)
                            }
                        )
                    }

                    AppScreen.SUPERVISOR_FLEET_MANAGEMENT -> {
                        val activeUser = currentUser ?: return@Box
                        FleetManagementScreen(
                            clients = clients,
                            vehicles = vehicles,
                            onBack = {
                                viewModel.navigateTo(
                                    when (activeUser.role) {
                                        UserRole.DRIVER -> AppScreen.DRIVER_HOME
                                        UserRole.SUPERVISOR -> AppScreen.SUPERVISOR_HOME
                                        UserRole.VENDOR -> AppScreen.VENDOR_DASHBOARD
                                    }
                                )
                            },
                            onAddClient = { name, addr, code -> viewModel.addNewClient(name, addr, code) },
                            onAddVehicle = { reg, model, fuel, client -> viewModel.addNewVehicle(reg, model, fuel, client) }
                        )
                    }

                    AppScreen.VENDOR_DASHBOARD -> {
                        val activeUser = currentUser ?: return@Box
                        VendorDashboardScreen(
                            vendorUser = activeUser,
                            clientsList = clients,
                            vehiclesList = vehicles,
                            allShifts = allShifts,
                            activeShifts = allActiveShifts,
                            allTrips = allTrips,
                            fuelEntries = allFuelEntries,
                            maintenanceList = allMaintenanceReports,
                            onNavigateToAudit = { viewModel.navigateTo(AppScreen.SUPERVISOR_AUDIT) },
                            onNavigateToFleet = { viewModel.navigateTo(AppScreen.SUPERVISOR_FLEET_MANAGEMENT) },
                            onNavigateToMaintenance = { viewModel.navigateTo(AppScreen.SUPERVISOR_MAINTENANCE) },
                            onNavigateToFuel = { viewModel.navigateTo(AppScreen.DRIVER_FUEL) },
                            onGenerateCsv = { viewModel.getExportCsv() }
                        )
                    }
                }
            }
        }
    }

    // Duty Action Modal Dialog
    if (activeDutyActionMode != null && currentUser != null) {
        DutyActionDialog(
            mode = activeDutyActionMode!!,
            user = currentUser!!,
            activeShift = activeShift,
            availableClients = clients,
            availableVehicles = vehicles,
            onDismiss = { activeDutyActionMode = null },
            onSubmitDriverStart = { client, reg, model, odo, photo, gpsLoc, gpsCoord, notes ->
                viewModel.startDriverDuty(client, reg, model, odo, photo, gpsLoc, gpsCoord, notes)
                activeDutyActionMode = null
            },
            onSubmitDriverEnd = { odo, photo, gpsLoc, gpsCoord, notes ->
                viewModel.endDriverDuty(odo, photo, gpsLoc, gpsCoord, notes)
                activeDutyActionMode = null
            },
            onSubmitSupervisorStart = { site, photo, gpsLoc, gpsCoord, notes ->
                viewModel.startSupervisorDuty(site, photo, gpsLoc, gpsCoord, notes)
                activeDutyActionMode = null
            },
            onSubmitSupervisorEnd = { photo, gpsLoc, gpsCoord, notes ->
                viewModel.endSupervisorDuty(photo, gpsLoc, gpsCoord, notes)
                activeDutyActionMode = null
            }
        )
    }

    // User Switcher Dialog
    if (showUserSwitcher) {
        UserSwitcherDialog(
            currentUser = currentUser,
            allUsers = users,
            onSelectUser = { user -> viewModel.quickLoginAs(user) },
            onLogout = { viewModel.logout() },
            onDismiss = { showUserSwitcher = false }
        )
    }
}
