package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.DutyShift
import com.example.data.model.MncClient
import com.example.data.model.UserAccount
import com.example.data.model.Vehicle
import com.example.location.GpsLocationResult
import com.example.ui.components.CameraCaptureDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DutyGreen
import com.example.ui.theme.FleetNavy
import com.example.ui.theme.TransitGold
import java.io.File

enum class DutyActionMode {
    DRIVER_START,
    DRIVER_END,
    SUPERVISOR_START,
    SUPERVISOR_END
}

@Composable
fun DutyActionDialog(
    mode: DutyActionMode,
    user: UserAccount,
    activeShift: DutyShift?,
    availableClients: List<MncClient>,
    availableVehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onSubmitDriverStart: (client: String, vehicleReg: String, model: String, startOdo: Double, photoUri: String, gpsLoc: String, gpsCoord: String, notes: String) -> Unit,
    onSubmitDriverEnd: (endOdo: Double, photoUri: String, gpsLoc: String, gpsCoord: String, notes: String) -> Unit,
    onSubmitSupervisorStart: (hubSite: String, photoUri: String, gpsLoc: String, gpsCoord: String, notes: String) -> Unit,
    onSubmitSupervisorEnd: (photoUri: String, gpsLoc: String, gpsCoord: String, notes: String) -> Unit
) {
    val context = LocalContext.current

    // Form states
    var selectedClient by remember {
        mutableStateOf(
            if (availableClients.isNotEmpty()) {
                user.assignedClientName.ifBlank { availableClients.first().name }
            } else "Google Tech Park"
        )
    }
    var clientDropdownExpanded by remember { mutableStateOf(false) }

    var selectedVehicleReg by remember {
        mutableStateOf(
            if (availableVehicles.isNotEmpty()) {
                user.assignedVehicleReg.ifBlank { availableVehicles.first().registrationNumber }
            } else "HR 26 CT 9012"
        )
    }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }

    val selectedVehicleObj = availableVehicles.find { it.registrationNumber == selectedVehicleReg }
    val vehicleModel = selectedVehicleObj?.model ?: "Maruti Suzuki Dzire"

    var odometerInput by remember {
        mutableStateOf(
            when (mode) {
                DutyActionMode.DRIVER_START -> {
                    selectedVehicleObj?.currentOdometerKm?.toInt()?.toString() ?: "48250"
                }
                DutyActionMode.DRIVER_END -> {
                    val prev = activeShift?.startOdometerKm ?: 48250.0
                    (prev + 45).toInt().toString()
                }
                else -> ""
            }
        )
    }

    var notesInput by remember { mutableStateOf("") }

    // Photo Capture States
    var capturedPhotoUri by remember { mutableStateOf("") }
    var capturedGps by remember { mutableStateOf<GpsLocationResult?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dialogTitle = when (mode) {
        DutyActionMode.DRIVER_START -> "Driver Shift Start • On-Duty"
        DutyActionMode.DRIVER_END -> "Driver Shift Close • Off-Duty"
        DutyActionMode.SUPERVISOR_START -> "Supervisor Shift Start • On-Duty"
        DutyActionMode.SUPERVISOR_END -> "Supervisor Shift Close • Off-Duty"
    }

    val photoPrompt = when (mode) {
        DutyActionMode.DRIVER_START -> "Mandatory Live Photo: Opening Odometer & Uniform"
        DutyActionMode.DRIVER_END -> "Mandatory Live Photo: Closing Odometer & Parked Cab"
        DutyActionMode.SUPERVISOR_START -> "Mandatory Live Photo: Site Supervisor Selfie / ID"
        DutyActionMode.SUPERVISOR_END -> "Mandatory Live Photo: Closing Site Status / Selfie"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = dialogTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${user.fullName} (${user.employeeId})",
                            fontSize = 12.sp,
                            color = TransitGold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode-Specific Form Inputs
                when (mode) {
                    DutyActionMode.DRIVER_START -> {
                        // MNC Client Dropdown
                        Text(
                            text = "Assigned MNC Client Account",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedClient,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, "Select", tint = Color.White)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { clientDropdownExpanded = true }
                                    .testTag("client_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = TransitGold,
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )
                            DropdownMenu(
                                expanded = clientDropdownExpanded,
                                onDismissRequest = { clientDropdownExpanded = false }
                            ) {
                                availableClients.forEach { client ->
                                    DropdownMenuItem(
                                        text = { Text(client.name) },
                                        onClick = {
                                            selectedClient = client.name
                                            clientDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Vehicle Dropdown
                        Text(
                            text = "Vehicle Registration Number",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = "$selectedVehicleReg ($vehicleModel)",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, "Select", tint = Color.White)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { vehicleDropdownExpanded = true }
                                    .testTag("vehicle_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = TransitGold,
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )
                            DropdownMenu(
                                expanded = vehicleDropdownExpanded,
                                onDismissRequest = { vehicleDropdownExpanded = false }
                            ) {
                                availableVehicles.forEach { veh ->
                                    DropdownMenuItem(
                                        text = { Text("${veh.registrationNumber} - ${veh.model} (${veh.fuelType})") },
                                        onClick = {
                                            selectedVehicleReg = veh.registrationNumber
                                            odometerInput = veh.currentOdometerKm.toInt().toString()
                                            vehicleDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Initial Odometer Reading
                        Text(
                            text = "Opening Odometer Reading (KM)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = odometerInput,
                            onValueChange = { odometerInput = it },
                            leadingIcon = {
                                Icon(Icons.Default.Speed, "Odometer", tint = DutyGreen)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("driver_start_odometer_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TransitGold,
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )
                    }

                    DutyActionMode.DRIVER_END -> {
                        val startOdo = activeShift?.startOdometerKm ?: 0.0
                        // Summary of current shift
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Active Shift: ${activeShift?.mncClientName ?: ""}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Cab: ${activeShift?.vehicleRegNumber ?: ""} (${activeShift?.vehicleModel ?: ""})",
                                    fontSize = 12.sp,
                                    color = TransitGold
                                )
                                Text(
                                    text = "Opening Odometer: $startOdo KM",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF93C5FD)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Closing Odometer Reading
                        Text(
                            text = "Closing Odometer Reading (KM)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = odometerInput,
                            onValueChange = { odometerInput = it },
                            leadingIcon = {
                                Icon(Icons.Default.Speed, "Odometer", tint = DutyGreen)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("driver_end_odometer_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TransitGold,
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )

                        val endOdo = odometerInput.toDoubleOrNull() ?: 0.0
                        if (endOdo >= startOdo) {
                            val diff = endOdo - startOdo
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ Calculated Shift Distance: %.1f KM".format(diff),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DutyGreen
                            )
                        }
                    }

                    DutyActionMode.SUPERVISOR_START -> {
                        Text(
                            text = "Primary Hub / Client Location",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedClient,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, "Select", tint = Color.White)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { clientDropdownExpanded = true }
                                    .testTag("supervisor_hub_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = TransitGold,
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )
                            DropdownMenu(
                                expanded = clientDropdownExpanded,
                                onDismissRequest = { clientDropdownExpanded = false }
                            ) {
                                availableClients.forEach { client ->
                                    DropdownMenuItem(
                                        text = { Text(client.name) },
                                        onClick = {
                                            selectedClient = client.name
                                            clientDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    DutyActionMode.SUPERVISOR_END -> {
                        Text(
                            text = "Active Hub: ${activeShift?.mncClientName ?: "CyberCity Hub"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Notes
                Text(
                    text = "Shift Remarks / Handover Notes (Optional)",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    placeholder = { Text("e.g. Clean vehicle, no dents, all safety tools present", color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = TransitGold,
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // MANDATORY CAMERA PHOTO CAPTURE SECTION
                Text(
                    text = photoPrompt,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TransitGold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (capturedPhotoUri.isNotBlank()) {
                    // Photo captured preview card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.5.dp, DutyGreen, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = DutyGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Photo Proof Captured & Stamped",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    val gps = capturedGps?.locationName ?: "GPS Coordinates Locked"
                                    Text(
                                        text = gps,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1
                                    )
                                }
                            }

                            Button(
                                onClick = { showCameraDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF334155),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Retake", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    // Mandatory photo button
                    Button(
                        onClick = { showCameraDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("launch_duty_camera_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TransitGold,
                            contentColor = FleetNavy
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture Live Photo",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Capture Mandatory Live Photo",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        color = AlertRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Final Duty Action Button
                val submitButtonColor = when (mode) {
                    DutyActionMode.DRIVER_START, DutyActionMode.SUPERVISOR_START -> DutyGreen
                    DutyActionMode.DRIVER_END, DutyActionMode.SUPERVISOR_END -> Color(0xFFEF4444)
                }
                val submitButtonLabel = when (mode) {
                    DutyActionMode.DRIVER_START -> "Confirm & Go On-Duty"
                    DutyActionMode.DRIVER_END -> "Confirm & Go Off-Duty"
                    DutyActionMode.SUPERVISOR_START -> "Confirm Supervisor Shift Start"
                    DutyActionMode.SUPERVISOR_END -> "Confirm Supervisor Shift Close"
                }

                Button(
                    onClick = {
                        if (capturedPhotoUri.isBlank()) {
                            errorMessage = "Mandatory: Please capture live photo proof with GPS before proceeding."
                            return@Button
                        }

                        val odo = odometerInput.toDoubleOrNull() ?: 0.0
                        val gpsLoc = capturedGps?.locationName ?: "Cyber City Hub"
                        val gpsCoord = capturedGps?.coordinatesFormatted ?: "28.4952° N, 77.0891° E"

                        when (mode) {
                            DutyActionMode.DRIVER_START -> {
                                onSubmitDriverStart(
                                    selectedClient,
                                    selectedVehicleReg,
                                    vehicleModel,
                                    odo,
                                    capturedPhotoUri,
                                    gpsLoc,
                                    gpsCoord,
                                    notesInput
                                )
                            }
                            DutyActionMode.DRIVER_END -> {
                                val startOdo = activeShift?.startOdometerKm ?: 0.0
                                if (odo < startOdo) {
                                    errorMessage = "Closing odometer ($odo KM) cannot be less than opening ($startOdo KM)."
                                    return@Button
                                }
                                onSubmitDriverEnd(
                                    odo,
                                    capturedPhotoUri,
                                    gpsLoc,
                                    gpsCoord,
                                    notesInput
                                )
                            }
                            DutyActionMode.SUPERVISOR_START -> {
                                onSubmitSupervisorStart(
                                    selectedClient,
                                    capturedPhotoUri,
                                    gpsLoc,
                                    gpsCoord,
                                    notesInput
                                )
                            }
                            DutyActionMode.SUPERVISOR_END -> {
                                onSubmitSupervisorEnd(
                                    capturedPhotoUri,
                                    gpsLoc,
                                    gpsCoord,
                                    notesInput
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_duty_action_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = submitButtonColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = submitButtonLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }

    // Camera Capture Sub-Dialog
    if (showCameraDialog) {
        val odoReading = if (mode == DutyActionMode.DRIVER_START || mode == DutyActionMode.DRIVER_END) {
            odometerInput
        } else null

        val entityTag = when (mode) {
            DutyActionMode.DRIVER_START, DutyActionMode.DRIVER_END -> "${user.fullName} • $selectedVehicleReg"
            DutyActionMode.SUPERVISOR_START, DutyActionMode.SUPERVISOR_END -> "${user.fullName} • Hub Supervisor"
        }

        CameraCaptureDialog(
            dutyActionTitle = dialogTitle,
            entityName = entityTag,
            odometerReading = odoReading,
            preferredHub = selectedClient,
            onDismiss = { showCameraDialog = false },
            onPhotoCaptured = { savedPath, gpsResult ->
                capturedPhotoUri = savedPath
                capturedGps = gpsResult
                errorMessage = null
                showCameraDialog = false
            }
        )
    }
}
