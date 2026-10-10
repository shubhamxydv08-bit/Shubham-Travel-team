package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DutyShift
import com.example.data.model.FuelEntry
import com.example.data.model.UserAccount
import com.example.data.model.Vehicle
import com.example.ui.components.CameraCaptureDialog
import com.example.ui.components.PhotoDetailDialog
import com.example.ui.theme.DutyGreen
import com.example.ui.theme.FleetNavy
import com.example.ui.theme.TransitGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelManagementScreen(
    user: UserAccount,
    activeShift: DutyShift?,
    availableVehicles: List<Vehicle>,
    allFuelEntries: List<FuelEntry>,
    onBack: () -> Unit,
    onSaveFuel: (vehicleReg: String, fuelType: String, qty: Double, amount: Double, odo: Double, station: String, receiptPhoto: String, gaugePhoto: String) -> Unit
) {
    BackHandler { onBack() }

    val fuelTypes = listOf("CNG", "Diesel", "Petrol", "EV Charging")

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedVehicle by remember {
        mutableStateOf(activeShift?.vehicleRegNumber ?: user.assignedVehicleReg.ifBlank { "HR 26 CT 9012" })
    }
    var selectedFuelType by remember { mutableStateOf("CNG") }
    var quantityInput by remember { mutableStateOf("10.0") }
    var amountInput by remember { mutableStateOf("850.0") }
    var odoInput by remember { mutableStateOf(activeShift?.startOdometerKm?.toInt()?.toString() ?: "48200") }
    var stationInput by remember { mutableStateOf("IGL Station Sector 29") }

    var receiptPhotoUri by remember { mutableStateOf("") }
    var gaugePhotoUri by remember { mutableStateOf("") }
    var cameraMode by remember { mutableStateOf<String?>(null) } // "RECEIPT" or "GAUGE"

    var inspectedPhotoUri by remember { mutableStateOf<String?>(null) }
    var inspectedPhotoTitle by remember { mutableStateOf("") }

    val totalSpent = allFuelEntries.sumOf { it.totalAmountInr }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fuel & Expense Management", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Receipt & Gauge Camera Verification", fontSize = 11.sp, color = Color(0xFFCBD5E1))
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
                            .testTag("open_add_fuel_button")
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Fuel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text("Total Recorded Fuel", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text("${allFuelEntries.size} Fillings", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Fleet Fuel Spent", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                "₹${totalSpent.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = TransitGold
                            )
                        }
                    }
                }
            }

            // Fuel Log Items
            items(allFuelEntries) { entry ->
                val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(entry.timestamp))
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
                                        .background(Color(0xFF78350F))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(entry.fuelType, fontSize = 11.sp, color = TransitGold, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(entry.vehicleRegNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Text(
                                "₹${entry.totalAmountInr.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = DutyGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${entry.quantityLiters} Units • Odometer: ${entry.odometerKm.toInt()} KM",
                                fontSize = 13.sp,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(dateStr, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }

                        if (entry.fuelStationName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Station: ${entry.fuelStationName}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Photo Proof chips
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .clickable {
                                        inspectedPhotoUri = entry.receiptPhotoUri.ifBlank { "asset://receipt.jpg" }
                                        inspectedPhotoTitle = "Fuel Slip Receipt • ₹${entry.totalAmountInr.toInt()}"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Receipt, null, tint = TransitGold, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Receipt Photo", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .clickable {
                                        inspectedPhotoUri = entry.gaugePhotoUri.ifBlank { "asset://gauge.jpg" }
                                        inspectedPhotoTitle = "Fuel Gauge & Odometer Proof"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, null, tint = DutyGreen, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gauge Photo", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Fuel Dialog
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
                    Text("Log Fuel Expense", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Mandatory Receipt & Gauge Camera Proof", fontSize = 12.sp, color = TransitGold)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedVehicle,
                            onValueChange = { selectedVehicle = it },
                            label = { Text("Vehicle Reg") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TransitGold,
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )

                        OutlinedTextField(
                            value = selectedFuelType,
                            onValueChange = { selectedFuelType = it },
                            label = { Text("Type") },
                            modifier = Modifier.weight(0.8f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TransitGold,
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = { quantityInput = it },
                            label = { Text("Liters / Kg") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TransitGold,
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )

                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = { Text("Amount (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                    OutlinedTextField(
                        value = odoInput,
                        onValueChange = { odoInput = it },
                        label = { Text("Current Odometer KM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TransitGold,
                            unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mandatory Receipt & Gauge Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { cameraMode = "RECEIPT" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (receiptPhotoUri.isNotBlank()) DutyGreen else Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("capture_receipt_photo_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (receiptPhotoUri.isNotBlank()) "Receipt ✓" else "Receipt Photo", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { cameraMode = "GAUGE" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gaugePhotoUri.isNotBlank()) DutyGreen else Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("capture_gauge_photo_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (gaugePhotoUri.isNotBlank()) "Gauge ✓" else "Gauge Photo", fontSize = 11.sp)
                        }
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
                                val qty = quantityInput.toDoubleOrNull() ?: 0.0
                                val amt = amountInput.toDoubleOrNull() ?: 0.0
                                val odo = odoInput.toDoubleOrNull() ?: 0.0
                                onSaveFuel(
                                    selectedVehicle,
                                    selectedFuelType,
                                    qty,
                                    amt,
                                    odo,
                                    stationInput,
                                    receiptPhotoUri,
                                    gaugePhotoUri
                                )
                                showAddDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DutyGreen),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("submit_fuel_entry_button")
                        ) {
                            Text("Submit Fuel Log", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (cameraMode != null) {
        val title = if (cameraMode == "RECEIPT") "FUEL RECEIPT BILL PHOTO" else "FUEL GAUGE / ODOMETER PHOTO"
        CameraCaptureDialog(
            dutyActionTitle = title,
            entityName = "$selectedVehicle • ₹$amountInput",
            odometerReading = odoInput,
            preferredHub = "Fuel Filling Station",
            onDismiss = { cameraMode = null },
            onPhotoCaptured = { savedPath, _ ->
                if (cameraMode == "RECEIPT") {
                    receiptPhotoUri = savedPath
                } else {
                    gaugePhotoUri = savedPath
                }
                cameraMode = null
            }
        )
    }

    if (inspectedPhotoUri != null) {
        PhotoDetailDialog(
            photoUri = inspectedPhotoUri!!,
            title = inspectedPhotoTitle,
            timestampFormatted = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.US).format(Date()),
            gpsLocation = "Approved Fuel Dispenser Hub",
            onDismiss = { inspectedPhotoUri = null }
        )
    }
}
