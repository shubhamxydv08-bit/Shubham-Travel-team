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
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
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
import com.example.data.model.TripLog
import com.example.data.model.UserAccount
import com.example.ui.theme.DutyGreen
import com.example.ui.theme.FleetNavy
import com.example.ui.theme.TransitGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSheetScreen(
    user: UserAccount,
    activeShift: DutyShift?,
    allTrips: List<TripLog>,
    onBack: () -> Unit,
    onSaveTrip: (tripType: String, route: String, pax: Int, startOdo: Double, endOdo: Double, remarks: String) -> Unit
) {
    BackHandler { onBack() }

    val tripTypes = listOf(
        "Employee Morning Pickup",
        "Employee Evening Drop",
        "Airport Executive Transfer",
        "Inter-Campus Shuttle",
        "Ad-hoc Emergency Trip"
    )

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTripType by remember { mutableStateOf(tripTypes[0]) }
    var tripTypeExpanded by remember { mutableStateOf(false) }

    var routeDescription by remember { mutableStateOf("") }
    var passengerCountInput by remember { mutableStateOf("4") }

    val defaultStartOdo = activeShift?.startOdometerKm ?: 48250.0
    var startOdoInput by remember { mutableStateOf(defaultStartOdo.toInt().toString()) }
    var endOdoInput by remember { mutableStateOf((defaultStartOdo + 35).toInt().toString()) }
    var remarksInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Trip-Sheet & Route Logs", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Digital Corporate Transit Sheet", fontSize = 11.sp, color = Color(0xFFCBD5E1))
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
                            .testTag("open_add_trip_button")
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Trip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text("Total Recorded Trips", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text("${allTrips.size} Trips", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Distance Logged", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                "%.1f KM".format(allTrips.sumOf { it.distanceKm }),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = DutyGreen
                            )
                        }
                    }
                }
            }

            // Trip Cards
            items(allTrips) { trip ->
                val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(trip.timestamp))
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
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(trip.tripType, fontSize = 11.sp, color = TransitGold, fontWeight = FontWeight.Bold)
                            }

                            Text(dateStr, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = trip.routeDescription,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Client: ${trip.mncClientName} • Cab: ${trip.vehicleRegNumber}",
                            fontSize = 12.sp,
                            color = Color(0xFF93C5FD)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${trip.passengerCount} Pax", fontSize = 12.sp, color = Color.White)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, null, tint = DutyGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${trip.startOdometer.toInt()} → ${trip.endOdometer.toInt()} (${trip.distanceKm.toInt()} KM)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DutyGreen
                                )
                            }
                        }

                        if (trip.remarks.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Note: ${trip.remarks}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Trip Dialog
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
                    Text("Add Digital Trip-Sheet Entry", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Logged under active client shift", fontSize = 12.sp, color = TransitGold)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Trip Type Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedTripType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Trip Category") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tripTypeExpanded = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TransitGold,
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )
                        DropdownMenu(
                            expanded = tripTypeExpanded,
                            onDismissRequest = { tripTypeExpanded = false }
                        ) {
                            tripTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedTripType = type
                                        tripTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = routeDescription,
                        onValueChange = { routeDescription = it },
                        label = { Text("Route Description") },
                        placeholder = { Text("e.g. DLF Phase 5 -> Sector 48 Campus", color = Color(0xFF64748B)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("route_description_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TransitGold,
                            unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = startOdoInput,
                            onValueChange = { startOdoInput = it },
                            label = { Text("Start KM") },
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
                            value = endOdoInput,
                            onValueChange = { endOdoInput = it },
                            label = { Text("End KM") },
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
                        value = passengerCountInput,
                        onValueChange = { passengerCountInput = it },
                        label = { Text("Passenger Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TransitGold,
                            unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                                val sOdo = startOdoInput.toDoubleOrNull() ?: 0.0
                                val eOdo = endOdoInput.toDoubleOrNull() ?: 0.0
                                val pax = passengerCountInput.toIntOrNull() ?: 1
                                onSaveTrip(
                                    selectedTripType,
                                    routeDescription.ifBlank { "MNC Corporate Transit" },
                                    pax,
                                    sOdo,
                                    eOdo,
                                    remarksInput
                                )
                                showAddDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DutyGreen),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("save_trip_submit_button")
                        ) {
                            Text("Save Trip", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
