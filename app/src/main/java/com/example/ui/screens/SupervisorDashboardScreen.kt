package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DutyShift
import com.example.data.model.MaintenanceReport
import com.example.data.model.MncClient
import com.example.data.model.ShiftStatus
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.Vehicle
import com.example.ui.components.PhotoDetailDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DutyGreen
import com.example.ui.theme.FleetNavy
import com.example.ui.theme.TransitGold
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SupervisorDashboardScreen(
    user: UserAccount,
    activeShift: DutyShift?,
    activeShiftsList: List<DutyShift>,
    allShiftsList: List<DutyShift>,
    vehiclesList: List<Vehicle>,
    clientsList: List<MncClient>,
    maintenanceList: List<MaintenanceReport>,
    onOpenDutyDialog: (DutyActionMode) -> Unit,
    onNavigateToAudit: () -> Unit,
    onNavigateToMaintenance: () -> Unit,
    onNavigateToFleet: () -> Unit
) {
    val isSupervisorOnDuty = activeShift?.status == ShiftStatus.ACTIVE
    var inspectedPhotoUri by remember { mutableStateOf<String?>(null) }
    var inspectedPhotoTitle by remember { mutableStateOf("") }

    // Live supervisor timer
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(activeShift) {
        if (activeShift != null) {
            while (true) {
                elapsedSeconds = ((System.currentTimeMillis() - activeShift.startTimestamp) / 1000).coerceAtLeast(0)
                delay(1000)
            }
        }
    }
    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val durationFormatted = "%02d:%02d:%02d".format(hours, minutes, seconds)

    // Fleet Analytics KPIs
    val activeDriverCount = activeShiftsList.count { it.userRole == UserRole.DRIVER }
    val activeSupervisorCount = activeShiftsList.count { it.userRole == UserRole.SUPERVISOR }
    val openAlerts = maintenanceList.count { it.status != "RESOLVED" }
    val totalCabsActive = vehiclesList.count { it.status == "ACTIVE" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. SUPERVISOR DEDICATED ON-DUTY & OFF-DUTY BUTTONS ---
        item {
            if (!isSupervisorOnDuty) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FleetNavy),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, TransitGold, RoundedCornerShape(20.dp))
                        .clickable { onOpenDutyDialog(DutyActionMode.SUPERVISOR_START) }
                        .testTag("supervisor_start_duty_large_card")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(TransitGold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FLEET SUPERVISOR ATTENDANCE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = TransitGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SUPERVISOR ON-DUTY",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Site Selfie & GPS Location Verification",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(TransitGold)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = FleetNavy,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, TransitGold, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = TransitGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SUPERVISOR SHIFT ACTIVE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = TransitGold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = durationFormatted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = DutyGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Hub: ${activeShift!!.mncClientName}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Site GPS: ${activeShift.startGpsLocationName}",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )

                        if (activeShift.startPhotoUri.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    inspectedPhotoUri = activeShift.startPhotoUri
                                    inspectedPhotoTitle = "Supervisor Shift Start Selfie Proof"
                                }
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = DutyGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Supervisor Selfie & ID Proof Stamped (Tap to view)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF93C5FD),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onOpenDutyDialog(DutyActionMode.SUPERVISOR_END) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("supervisor_end_duty_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlertRed,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "End Duty",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SUPERVISOR OFF-DUTY • CLOSE SHIFT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // --- 2. REAL-TIME FLEET TELEMETRY METRICS ---
        item {
            Text(
                text = "REAL-TIME FLEET OPERATIONS METRICS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Active Cabs
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Active Cabs", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "$totalCabsActive / ${vehiclesList.size}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DutyGreen
                        )
                    }
                }

                // Drivers On-Duty
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Drivers On-Duty", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "$activeDriverCount Active",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TransitGold
                        )
                    }
                }

                // Maintenance Alerts
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToMaintenance() }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Defect Alerts", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "$openAlerts Open",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (openAlerts > 0) Color(0xFFEF4444) else DutyGreen
                        )
                    }
                }
            }
        }

        // --- 3. SUPERVISOR ACTION MODULES ---
        item {
            Text(
                text = "MANAGEMENT & AUDIT MODULES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Audit Dashboard & Export
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToAudit() }
                        .testTag("nav_audit_dashboard_button")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Assignment, null, tint = TransitGold, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("MNC Audit & Reports", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Export CSV Logs", fontSize = 11.sp, color = Color(0xFFDBEAFE))
                    }
                }

                // Maintenance Hub
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToMaintenance() }
                        .testTag("nav_maintenance_center_button")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Build, null, tint = Color(0xFFEF4444), modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Maintenance Hub", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Service & Grounding", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }

                // Fleet & Client Master
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToFleet() }
                        .testTag("nav_fleet_vehicles_button")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.DirectionsCar, null, tint = DutyGreen, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fleet Cabs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("MNC Quotas", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }

        // --- 4. LIVE ACTIVE ROSTER: DRIVERS & SUPERVISORS ACROSS SITES ---
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "REAL-TIME ON-DUTY ROSTER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = "${activeShiftsList.size} Staff On-Duty",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DutyGreen
                )
            }
        }

        if (activeShiftsList.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .padding(24.dp)
                ) {
                    Text(
                        text = "No staff currently on active duty",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            items(activeShiftsList) { shift ->
                val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date(shift.startTimestamp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (shift.userRole == UserRole.DRIVER) TransitGold else FleetNavy)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = shift.userRole.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (shift.userRole == UserRole.DRIVER) FleetNavy else Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = shift.userName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Started $timeStr",
                                    fontSize = 11.sp,
                                    color = Color(0xFF93C5FD)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "MNC: ${shift.mncClientName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TransitGold
                            )

                            if (shift.userRole == UserRole.DRIVER) {
                                Text(
                                    text = "${shift.vehicleRegNumber} • ${shift.startOdometerKm.toInt()} KM",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, null, tint = DutyGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = shift.startGpsLocationName,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }

                        if (shift.startPhotoUri.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    inspectedPhotoUri = shift.startPhotoUri
                                    inspectedPhotoTitle = "${shift.userName} • Start Photo Proof"
                                }
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = DutyGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mandatory Camera Photo Stamped (Tap to view)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF93C5FD)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (inspectedPhotoUri != null) {
        PhotoDetailDialog(
            photoUri = inspectedPhotoUri!!,
            title = inspectedPhotoTitle,
            timestampFormatted = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.US).format(Date()),
            gpsLocation = "Cyber Hub Verification Point",
            onDismiss = { inspectedPhotoUri = null }
        )
    }
}
