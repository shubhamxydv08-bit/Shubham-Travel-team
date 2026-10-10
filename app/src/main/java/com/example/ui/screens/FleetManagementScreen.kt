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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.MncClient
import com.example.data.model.Vehicle
import com.example.ui.theme.DutyGreen
import com.example.ui.theme.FleetNavy
import com.example.ui.theme.TransitGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetManagementScreen(
    clients: List<MncClient>,
    vehicles: List<Vehicle>,
    onBack: () -> Unit,
    onAddClient: (name: String, address: String, code: String) -> Unit,
    onAddVehicle: (reg: String, model: String, fuel: String, client: String) -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableStateOf("VEHICLES") } // "VEHICLES" or "CLIENTS"

    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var newVehicleReg by remember { mutableStateOf("") }
    var newVehicleModel by remember { mutableStateOf("Maruti Suzuki Dzire Tour") }
    var newVehicleFuel by remember { mutableStateOf("CNG") }
    var newVehicleClient by remember { mutableStateOf(if (clients.isNotEmpty()) clients.first().name else "Google Tech Park") }

    var showAddClientDialog by remember { mutableStateOf(false) }
    var newClientName by remember { mutableStateOf("") }
    var newClientAddress by remember { mutableStateOf("") }
    var newClientCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MNC Clients & Fleet Roster", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Vehicle Tagging & Client Quotas", fontSize = 11.sp, color = Color(0xFFCBD5E1))
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
                            if (selectedTab == "VEHICLES") showAddVehicleDialog = true else showAddClientDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TransitGold, contentColor = FleetNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("add_fleet_item_button")
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedTab == "VEHICLES") "Add Cab" else "Add Client", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FleetNavy)
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .padding(4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == "VEHICLES") TransitGold else Color.Transparent)
                        .clickable { selectedTab = "VEHICLES" }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        "Fleet Cabs (${vehicles.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedTab == "VEHICLES") FleetNavy else Color.White
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == "CLIENTS") TransitGold else Color.Transparent)
                        .clickable { selectedTab = "CLIENTS" }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        "MNC Clients (${clients.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedTab == "CLIENTS") FleetNavy else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == "VEHICLES") {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(vehicles) { veh ->
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
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(veh.registrationNumber, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF0F172A))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(veh.fuelType, fontSize = 10.sp, color = TransitGold, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(veh.model, fontSize = 13.sp, color = Color(0xFFCBD5E1))
                                    Text(
                                        text = "Client: ${veh.assignedMncClient.ifBlank { "Unassigned" }}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF93C5FD)
                                    )
                                    if (veh.assignedDriverName.isNotBlank()) {
                                        Text(
                                            text = "Driver: ${veh.assignedDriverName}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (veh.status) {
                                                    "ACTIVE" -> DutyGreen
                                                    "GROUNDED" -> Color(0xFFDC2626)
                                                    else -> Color(0xFFD97706)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(veh.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "${veh.currentOdometerKm.toInt()} KM",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TransitGold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(clients) { client ->
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
                                        Icon(Icons.Default.Business, null, tint = TransitGold, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(client.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF0F172A))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(client.code, fontSize = 10.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(client.campusAddress, fontSize = 12.sp, color = Color(0xFFCBD5E1))

                                if (client.contactPerson.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Contact: ${client.contactPerson} (${client.contactPhone})",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Allocated Cab Quota: ${client.activeCabQuota} Vehicles",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DutyGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Vehicle Dialog
    if (showAddVehicleDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddVehicleDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Corporate Fleet Vehicle", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newVehicleReg,
                        onValueChange = { newVehicleReg = it },
                        label = { Text("Registration No (e.g. DL 01 AB 9988)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = TransitGold, unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newVehicleModel,
                        onValueChange = { newVehicleModel = it },
                        label = { Text("Vehicle Model (e.g. Toyota Innova)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = TransitGold, unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newVehicleFuel,
                            onValueChange = { newVehicleFuel = it },
                            label = { Text("Fuel (CNG/Diesel/EV)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = TransitGold, unfocusedBorderColor = Color(0xFF475569)
                            )
                        )

                        OutlinedTextField(
                            value = newVehicleClient,
                            onValueChange = { newVehicleClient = it },
                            label = { Text("Assigned MNC") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = TransitGold, unfocusedBorderColor = Color(0xFF475569)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showAddVehicleDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (newVehicleReg.isNotBlank()) {
                                    onAddVehicle(newVehicleReg, newVehicleModel, newVehicleFuel, newVehicleClient)
                                    showAddVehicleDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DutyGreen),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text("Save Vehicle", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Add Client Dialog
    if (showAddClientDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddClientDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add MNC Client Account", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newClientName,
                        onValueChange = { newClientName = it },
                        label = { Text("Client Company Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = TransitGold, unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newClientAddress,
                        onValueChange = { newClientAddress = it },
                        label = { Text("Campus / Office Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = TransitGold, unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newClientCode,
                        onValueChange = { newClientCode = it },
                        label = { Text("Account Code (e.g. MSFT-GUR)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = TransitGold, unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showAddClientDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (newClientName.isNotBlank()) {
                                    onAddClient(newClientName, newClientAddress, newClientCode)
                                    showAddClientDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DutyGreen),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text("Save Client", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
