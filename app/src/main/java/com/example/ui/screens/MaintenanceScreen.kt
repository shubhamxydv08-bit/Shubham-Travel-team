package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MaintenanceReport
import com.example.data.model.UserAccount
import com.example.data.model.Vehicle
import com.example.ui.components.CameraCaptureDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DutyGreen
import com.example.ui.theme.FleetNavy
import com.example.ui.theme.TransitGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    user: UserAccount,
    vehicles: List<Vehicle>,
    allReports: List<MaintenanceReport>,
    onBack: () -> Unit,
    onReportMaintenance: (reg: String, category: String, priority: String, desc: String, odo: Double, photo: String) -> Unit,
    onResolveReport: (reportId: Long, reg: String) -> Unit
) {
    BackHandler { onBack() }

    val categories = listOf(
        "Tyre Puncture / Wear",
        "Brake Pad & Lining Check",
        "AC Cooling Malfunction",
        "Scheduled Oil Service Due",
        "Body Dent / Scratch Inspection",
        "Battery / Electrical Fault"
    )

    val priorities = listOf(
        "HIGH (Ground Vehicle Immediately)",
        "MEDIUM (Attend within 24h)",
        "LOW (Routine Next Scheduled Service)"
    )

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedVehicle by remember { mutableStateOf(if (vehicles.isNotEmpty()) vehicles.first().registrationNumber else "HR 26 CT 9012") }
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var selectedPriority by remember { mutableStateOf(priorities[1]) }
    var priorityExpanded by remember { mutableStateOf(false) }

    var descriptionInput by remember { mutableStateOf("") }
    var odoInput by remember { mutableStateOf("48250") }
    var photoUri by remember { mutableStateOf("") }
    var showCamera by remember { mutableStateOf(false) }

    val openCount = allReports.count { it.status != "RESOLVED" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fleet Maintenance & Service", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Breakdown, Defects & Grounding Alerts", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TransitGold, contentColor = FleetNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("report_issue_button")
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Report Issue", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FleetNavy)
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Active Maintenance Alerts", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                "$openCount Open Tickets",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = if (openCount > 0) AlertRed else DutyGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Reported", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                "${allReports.size} Issues",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Report Tickets
            items(allReports) { report ->
                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(report.timestamp))
                val isResolved = report.status == "RESOLVED"

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
                                        .background(
                                            if (report.priority.contains("HIGH")) Color(0xFF7F1D1D) else Color(0xFF1E3A8A)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = report.priority.take(15) + "...",
                                        fontSize = 11.sp,
                                        color = if (report.priority.contains("HIGH")) Color(0xFFFCA5A5) else Color(0xFF93C5FD),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    report.vehicleRegNumber,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isResolved) DutyGreen else TransitGold)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = report.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isResolved) Color.White else FleetNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = report.issueCategory,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TransitGold
                        )

                        Text(
                            text = report.description,
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Reported by: ${report.reporterName} (${report.reporterRole})",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(dateStr, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }

                        if (!isResolved) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onResolveReport(report.id, report.vehicleRegNumber) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DutyGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Resolved & Return to Active Service", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Report Maintenance Dialog
    if (showAddDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("Report Vehicle Maintenance / Defect", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Alerts dispatch team and grounds vehicle if HIGH", fontSize = 12.sp, color = TransitGold)

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = selectedVehicle,
                        onValueChange = { selectedVehicle = it },
                        label = { Text("Vehicle Registration") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TransitGold,
                            unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Issue Category
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Issue Category") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryExpanded = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TransitGold,
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Priority
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedPriority,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority Level") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { priorityExpanded = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TransitGold,
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )
                        DropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false }
                        ) {
                            priorities.forEach { prio ->
                                DropdownMenuItem(
                                    text = { Text(prio) },
                                    onClick = {
                                        selectedPriority = prio
                                        priorityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Defect Description / Notes") },
                        placeholder = { Text("e.g. Nail in rear left tyre, pressure dropping fast", color = Color(0xFF64748B)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TransitGold,
                            unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showCamera = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (photoUri.isNotBlank()) DutyGreen else Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (photoUri.isNotBlank()) "Defect Photo Attached ✓" else "Capture Defect Photo Proof", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showAddDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                onReportMaintenance(
                                    selectedVehicle,
                                    selectedCategory,
                                    selectedPriority,
                                    descriptionInput.ifBlank { "Service alert logged by ${user.fullName}" },
                                    odoInput.toDoubleOrNull() ?: 48250.0,
                                    photoUri
                                )
                                showAddDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("submit_maintenance_report_button")
                        ) {
                            Text("Log Alert", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showCamera) {
        CameraCaptureDialog(
            dutyActionTitle = "MAINTENANCE DEFECT PHOTO PROOF",
            entityName = "$selectedVehicle • $selectedCategory",
            odometerReading = odoInput,
            preferredHub = "Fleet Maintenance Workshop",
            onDismiss = { showCamera = false },
            onPhotoCaptured = { savedPath, _ ->
                photoUri = savedPath
                showCamera = false
            }
        )
    }
}
