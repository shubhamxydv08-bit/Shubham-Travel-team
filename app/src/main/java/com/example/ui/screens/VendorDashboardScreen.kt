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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.data.model.FuelEntry
import com.example.data.model.MaintenanceReport
import com.example.data.model.MncClient
import com.example.data.model.ShiftStatus
import com.example.data.model.TripLog
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.Vehicle
import com.example.ui.components.AuditExportDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DutyGreen
import com.example.ui.theme.FleetNavy
import com.example.ui.theme.TransitGold

@Composable
fun VendorDashboardScreen(
    vendorUser: UserAccount,
    clientsList: List<MncClient>,
    vehiclesList: List<Vehicle>,
    allShifts: List<DutyShift>,
    activeShifts: List<DutyShift>,
    allTrips: List<TripLog>,
    fuelEntries: List<FuelEntry>,
    maintenanceList: List<MaintenanceReport>,
    onNavigateToAudit: () -> Unit,
    onNavigateToFleet: () -> Unit,
    onNavigateToMaintenance: () -> Unit,
    onNavigateToFuel: () -> Unit,
    onGenerateCsv: () -> String
) {
    var showExportDialog by remember { mutableStateOf(false) }

    // Financial & Fleet Aggregations
    val totalCabs = vehiclesList.size
    val activeCabs = vehiclesList.count { it.status == "ACTIVE" }
    val groundedCabs = vehiclesList.count { it.status == "GROUNDED" }
    val inServiceCabs = vehiclesList.count { it.status == "IN_SERVICE" }

    val totalDistanceTraveledKm = allShifts.sumOf { it.totalKmTraveled } + allTrips.sumOf { it.distanceKm }
    val totalFuelExpense = fuelEntries.sumOf { it.totalAmountInr }

    // Estimated billing calculation (Standard corporate MNC transport rate: ₹18/km)
    val corporateRatePerKm = 18.0
    val estimatedGrossBilling = totalDistanceTraveledKm * corporateRatePerKm
    val netMargin = (estimatedGrossBilling - totalFuelExpense).coerceAtLeast(0.0)

    val activeDriversCount = activeShifts.count { it.userRole == UserRole.DRIVER }
    val activeSupervisorsCount = activeShifts.count { it.userRole == UserRole.SUPERVISOR }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. VENDOR ENTERPRISE HEADER CARD ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FleetNavy),
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
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TransitGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = "Vendor Logo",
                                    tint = FleetNavy,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SHUBHAM TRAVEL TEAM",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Principal Fleet Vendor Partner Portal",
                                    fontSize = 11.sp,
                                    color = TransitGold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E3A8A))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VENDOR OWNER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF93C5FD)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Vendor Operator: ${vendorUser.fullName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Emp ID: ${vendorUser.employeeId} • Multi-MNC Corporate Contract Dispatch",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // --- 2. VENDOR FINANCIAL & BILLING METRICS ---
        item {
            Text(
                text = "COMMERCIAL & BILLING OVERVIEW",
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
                // Gross Billing
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Est. Gross Billing", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "₹${"%,d".format(estimatedGrossBilling.toInt())}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = DutyGreen
                        )
                        Text(
                            text = "@ ₹18/KM Contract Rate",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                // Fuel Cost
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Fleet Fuel Spent", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "₹${"%,d".format(totalFuelExpense.toInt())}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TransitGold
                        )
                        Text(
                            text = "${fuelEntries.size} Verified Slips",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                // Net Margin
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Gross Operating", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "₹${"%,d".format(netMargin.toInt())}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF93C5FD)
                        )
                        Text(
                            text = "Net Surplus",
                            fontSize = 10.sp,
                            color = DutyGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // --- 3. FLEET ASSET STATUS TELEMETRY ---
        item {
            Text(
                text = "FLEET VEHICLE ASSET STATUS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Total Fleet Size", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("$totalCabs Commercial Cabs", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Distance Dispatched", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("%.1f KM".format(totalDistanceTraveledKm), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TransitGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(DutyGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$activeCabs Active on Roster", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TransitGold))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$inServiceCabs In Service", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AlertRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$groundedCabs Grounded", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // --- 4. VENDOR QUICK ACTION MODULES ---
        item {
            Text(
                text = "VENDOR MANAGEMENT MODULES",
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
                // Audit Reports
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToAudit() }
                        .testTag("vendor_nav_audit_button")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.Assessment, null, tint = TransitGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Audit & Billing", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Export Compliance", fontSize = 11.sp, color = Color(0xFFDBEAFE))
                    }
                }

                // Fleet Roster
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToFleet() }
                        .testTag("vendor_nav_fleet_button")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.DirectionsCar, null, tint = DutyGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Fleet Cabs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Manage Quotas", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }

                // Fuel Logs
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToFuel() }
                        .testTag("vendor_nav_fuel_button")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.LocalGasStation, null, tint = TransitGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Fuel Audit", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Verify Slips", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }

        // --- 5. MNC CLIENT CONTRACTS BREAKDOWN ---
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ACTIVE MNC CLIENT ACCOUNTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = "${clientsList.size} Accounts",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TransitGold
                )
            }
        }

        items(clientsList) { client ->
            val assignedVehicles = vehiclesList.filter { it.assignedMncClient.equals(client.name, ignoreCase = true) }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(client.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(client.code, fontSize = 10.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(client.campusAddress, fontSize = 11.sp, color = Color(0xFFCBD5E1), maxLines = 1)

                        if (assignedVehicles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Dedicated Cabs: ${assignedVehicles.joinToString { it.registrationNumber }}",
                                fontSize = 11.sp,
                                color = TransitGold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${client.activeCabQuota} Cabs",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = DutyGreen
                        )
                        Text("Active Quota", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }

        // --- 6. EXPORT FINANCIAL REPORT BUTTON ---
        item {
            Button(
                onClick = { showExportDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("vendor_export_audit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DutyGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Description, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export Vendor Financial & MNC Audit Report",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showExportDialog) {
        AuditExportDialog(
            shifts = allShifts,
            csvContent = onGenerateCsv(),
            onDismiss = { showExportDialog = false }
        )
    }
}
