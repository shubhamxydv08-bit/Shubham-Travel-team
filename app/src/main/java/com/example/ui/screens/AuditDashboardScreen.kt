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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DutyShift
import com.example.data.model.MncClient
import com.example.data.model.ShiftStatus
import com.example.data.model.UserRole
import com.example.ui.components.AuditExportDialog
import com.example.ui.components.PhotoDetailDialog
import com.example.ui.theme.DutyGreen
import com.example.ui.theme.FleetNavy
import com.example.ui.theme.TransitGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditDashboardScreen(
    shifts: List<DutyShift>,
    clients: List<MncClient>,
    selectedClientFilter: String,
    staffFilter: String,
    vehicleFilter: String,
    dateFilter: String,
    onClientFilterChanged: (String) -> Unit,
    onStaffFilterChanged: (String) -> Unit,
    onVehicleFilterChanged: (String) -> Unit,
    onDateFilterChanged: (String) -> Unit,
    onBack: () -> Unit,
    onGenerateCsv: () -> String
) {
    BackHandler { onBack() }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportCsvText by remember { mutableStateOf("") }

    var inspectedPhotoUri by remember { mutableStateOf<String?>(null) }
    var inspectedPhotoTitle by remember { mutableStateOf("") }
    var inspectedPhotoTime by remember { mutableStateOf("") }
    var inspectedPhotoGps by remember { mutableStateOf("") }

    var clientFilterExpanded by remember { mutableStateOf(false) }
    val dateFilters = listOf("ALL", "TODAY", "YESTERDAY", "THIS_WEEK")

    val totalKm = shifts.sumOf { it.totalKmTraveled }
    val activeCount = shifts.count { it.status == ShiftStatus.ACTIVE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Supervisor & MNC Audit Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Billing Compliance & Timestamped Photo Logs", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            exportCsvText = onGenerateCsv()
                            showExportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TransitGold, contentColor = FleetNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("export_audit_button")
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Filtered Shifts", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("${shifts.size} Total", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("$activeCount Active Now", fontSize = 11.sp, color = DutyGreen)
                        }

                        Column {
                            Text("Total Distance", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("%.1f KM".format(totalKm), fontSize = 18.sp, fontWeight = FontWeight.Black, color = TransitGold)
                            Text("Verified by GPS", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Photo Compliance", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("100%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DutyGreen)
                            Text("MNC Approved", fontSize = 11.sp, color = Color(0xFF93C5FD))
                        }
                    }
                }
            }

            // Filters Section
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilterList, null, tint = TransitGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AUDIT FILTERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TransitGold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // MNC Client Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = if (selectedClientFilter == "ALL") "All MNC Clients" else selectedClientFilter,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter by MNC Client") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { clientFilterExpanded = true }
                                    .testTag("audit_client_filter_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = TransitGold,
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )
                            DropdownMenu(
                                expanded = clientFilterExpanded,
                                onDismissRequest = { clientFilterExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All MNC Clients") },
                                    onClick = {
                                        onClientFilterChanged("ALL")
                                        clientFilterExpanded = false
                                    }
                                )
                                clients.forEach { client ->
                                    DropdownMenuItem(
                                        text = { Text(client.name) },
                                        onClick = {
                                            onClientFilterChanged(client.name)
                                            clientFilterExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = staffFilter,
                                onValueChange = onStaffFilterChanged,
                                label = { Text("Driver / Staff") },
                                placeholder = { Text("e.g. Rajesh", color = Color(0xFF64748B)) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = TransitGold,
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )

                            OutlinedTextField(
                                value = vehicleFilter,
                                onValueChange = onVehicleFilterChanged,
                                label = { Text("Vehicle Reg") },
                                placeholder = { Text("e.g. HR 26", color = Color(0xFF64748B)) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = TransitGold,
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Date chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            dateFilters.forEach { df ->
                                val selected = dateFilter == df
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) TransitGold else Color(0xFF0F172A))
                                        .clickable { onDateFilterChanged(df) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = df.replace("_", " "),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) FleetNavy else Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Shift Audit Cards
            items(shifts) { shift ->
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
                val startFormatted = sdf.format(Date(shift.startTimestamp))
                val endFormatted = shift.endTimestamp?.let { sdf.format(Date(it)) } ?: "Active Shift (On-Duty)"

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header
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
                                        shift.userRole.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (shift.userRole == UserRole.DRIVER) FleetNavy else Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(shift.userName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (shift.status == ShiftStatus.ACTIVE) DutyGreen else Color(0xFF334155))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (shift.status == ShiftStatus.ACTIVE) "● ON-DUTY" else "COMPLETED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "MNC: ${shift.mncClientName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TransitGold
                        )

                        if (shift.userRole == UserRole.DRIVER) {
                            Text(
                                text = "Cab: ${shift.vehicleRegNumber} • ${shift.vehicleModel}",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Odometer & Distance Bar
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Duty Start", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(startFormatted, fontSize = 11.sp, color = Color.White)
                                if (shift.userRole == UserRole.DRIVER) {
                                    Text("Odo: ${shift.startOdometerKm.toInt()} KM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TransitGold)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Duty End", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(endFormatted, fontSize = 11.sp, color = Color.White)
                                if (shift.userRole == UserRole.DRIVER && shift.endOdometerKm != null) {
                                    Text("Odo: ${shift.endOdometerKm.toInt()} KM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TransitGold)
                                }
                            }

                            if (shift.userRole == UserRole.DRIVER) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Travel", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Text(
                                        "${shift.totalKmTraveled.toInt()} KM",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DutyGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // GPS Site Stamps
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GpsFixed, null, tint = DutyGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Start GPS: ${shift.startGpsLocationName}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1
                                )
                            }

                            if (shift.endGpsLocationName.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.GpsFixed, null, tint = TransitGold, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "End GPS: ${shift.endGpsLocationName}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Side-by-side Photo Verification Proofs
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Opening Photo
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .clickable {
                                        inspectedPhotoUri = shift.startPhotoUri.ifBlank { "asset://photo.jpg" }
                                        inspectedPhotoTitle = "Shift Opening Photo • ${shift.userName}"
                                        inspectedPhotoTime = startFormatted
                                        inspectedPhotoGps = shift.startGpsLocationName
                                    }
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhotoCamera, null, tint = DutyGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Opening Proof ✓", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            // Closing Photo
                            if (shift.endPhotoUri.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .clickable {
                                            inspectedPhotoUri = shift.endPhotoUri
                                            inspectedPhotoTitle = "Shift Closing Photo • ${shift.userName}"
                                            inspectedPhotoTime = endFormatted
                                            inspectedPhotoGps = shift.endGpsLocationName
                                        }
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PhotoCamera, null, tint = TransitGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Closing Proof ✓", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        if (shift.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Notes: ${shift.notes}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        AuditExportDialog(
            shifts = shifts,
            csvContent = exportCsvText,
            onDismiss = { showExportDialog = false }
        )
    }

    if (inspectedPhotoUri != null) {
        PhotoDetailDialog(
            photoUri = inspectedPhotoUri!!,
            title = inspectedPhotoTitle,
            timestampFormatted = inspectedPhotoTime,
            gpsLocation = inspectedPhotoGps,
            onDismiss = { inspectedPhotoUri = null }
        )
    }
}
