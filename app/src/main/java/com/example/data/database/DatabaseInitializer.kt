package com.example.data.database

import com.example.data.dao.ShubhamDao
import com.example.data.model.DutyShift
import com.example.data.model.FuelEntry
import com.example.data.model.MaintenanceReport
import com.example.data.model.MncClient
import com.example.data.model.ShiftStatus
import com.example.data.model.TripLog
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun seedDatabaseIfEmpty(dao: ShubhamDao) = withContext(Dispatchers.IO) {
        val existingClients = dao.getAllMncClients().firstOrNull()
        if (!existingClients.isNullOrEmpty()) {
            return@withContext
        }

        // Seed MNC Clients
        val clients = listOf(
            MncClient(
                name = "Google Tech Park",
                code = "GOOG-GUR",
                campusAddress = "Unitech Cyber Park, Sector 39, Gurgaon",
                activeCabQuota = 18,
                contactPerson = "Sameer Anand (Facilities)",
                contactPhone = "+91 98101 22334"
            ),
            MncClient(
                name = "Microsoft IDC",
                code = "MSFT-DLF",
                campusAddress = "Cyber City Tower B, DLF Phase 2, Gurgaon",
                activeCabQuota = 15,
                contactPerson = "Pooja Mehta (Transport Lead)",
                contactPhone = "+91 98188 33445"
            ),
            MncClient(
                name = "Amazon Corporate",
                code = "AMZN-GCR",
                campusAddress = "Golf Course Ext Rd, Sector 65, Gurgaon",
                activeCabQuota = 22,
                contactPerson = "Gaurav Sen (Fleet Ops)",
                contactPhone = "+91 98200 44556"
            ),
            MncClient(
                name = "Deloitte Towers",
                code = "DEL-SEC43",
                campusAddress = "Golf Course Road, Sector 43, Gurgaon",
                activeCabQuota = 14,
                contactPerson = "Rohit Verma (Admin)",
                contactPhone = "+91 98111 66778"
            ),
            MncClient(
                name = "Tata Consultancy Services",
                code = "TCS-DLF3",
                campusAddress = "DLF CyberCity Phase 3, Gurgaon",
                activeCabQuota = 25,
                contactPerson = "Anjali Nair (Workplace Services)",
                contactPhone = "+91 98711 88990"
            ),
            MncClient(
                name = "Accenture Operations",
                code = "ACN-SEC48",
                campusAddress = "Sector 48, Sohna Road, Gurgaon",
                activeCabQuota = 16,
                contactPerson = "Vikas Chopra (Transport)",
                contactPhone = "+91 99100 11223"
            )
        )
        dao.insertMncClients(clients)

        // Seed Corporate Vehicles
        val vehicles = listOf(
            Vehicle(
                registrationNumber = "HR 26 CT 9012",
                model = "Maruti Suzuki Dzire Tour",
                fuelType = "CNG",
                seatingCapacity = 4,
                currentOdometerKm = 48250.0,
                assignedDriverName = "Rajesh Kumar",
                assignedMncClient = "Google Tech Park",
                status = "ACTIVE"
            ),
            Vehicle(
                registrationNumber = "DL 01 AB 1234",
                model = "Toyota Innova Crysta",
                fuelType = "Diesel",
                seatingCapacity = 6,
                currentOdometerKm = 72400.0,
                assignedDriverName = "Vikram Sharma",
                assignedMncClient = "Microsoft IDC",
                status = "ACTIVE"
            ),
            Vehicle(
                registrationNumber = "DL 03 XY 5678",
                model = "Tata Tigor EV",
                fuelType = "EV",
                seatingCapacity = 4,
                currentOdometerKm = 21800.0,
                assignedDriverName = "Suresh Yadav",
                assignedMncClient = "Amazon Corporate",
                status = "ACTIVE"
            ),
            Vehicle(
                registrationNumber = "HR 55 BD 3344",
                model = "Maruti Suzuki Ertiga",
                fuelType = "CNG",
                seatingCapacity = 6,
                currentOdometerKm = 54120.0,
                assignedDriverName = "Manoj Tiwari",
                assignedMncClient = "Deloitte Towers",
                status = "ACTIVE"
            ),
            Vehicle(
                registrationNumber = "UP 16 ER 7890",
                model = "Hyundai Aura Prime",
                fuelType = "CNG",
                seatingCapacity = 4,
                currentOdometerKm = 39900.0,
                assignedDriverName = "Deepak Verma",
                assignedMncClient = "Tata Consultancy Services",
                status = "ACTIVE"
            ),
            Vehicle(
                registrationNumber = "DL 09 CF 4455",
                model = "Mahindra Marazzo",
                fuelType = "Diesel",
                seatingCapacity = 7,
                currentOdometerKm = 61200.0,
                assignedDriverName = "Rakesh Chauhan",
                assignedMncClient = "Accenture Operations",
                status = "IN_SERVICE"
            )
        )
        dao.insertVehicles(vehicles)

        // Seed Pre-registered Drivers & Supervisors
        val users = listOf(
            UserAccount(
                phoneNumber = "+91 98765 43210",
                fullName = "Rajesh Kumar",
                role = UserRole.DRIVER,
                employeeId = "ST-D101",
                assignedClientName = "Google Tech Park",
                assignedVehicleReg = "HR 26 CT 9012"
            ),
            UserAccount(
                phoneNumber = "+91 98111 22334",
                fullName = "Vikram Sharma",
                role = UserRole.DRIVER,
                employeeId = "ST-D104",
                assignedClientName = "Microsoft IDC",
                assignedVehicleReg = "DL 01 AB 1234"
            ),
            UserAccount(
                phoneNumber = "+91 98222 11000",
                fullName = "Suresh Yadav",
                role = UserRole.DRIVER,
                employeeId = "ST-D108",
                assignedClientName = "Amazon Corporate",
                assignedVehicleReg = "DL 03 XY 5678"
            ),
            UserAccount(
                phoneNumber = "+91 99888 77665",
                fullName = "Amit Singh",
                role = UserRole.SUPERVISOR,
                employeeId = "ST-S201",
                assignedClientName = "CyberCity Hub All Clients"
            ),
            UserAccount(
                phoneNumber = "+91 98333 44556",
                fullName = "Sunita Rao",
                role = UserRole.SUPERVISOR,
                employeeId = "ST-S205",
                assignedClientName = "Golf Course Rd Sector 43 Hub"
            ),
            UserAccount(
                phoneNumber = "+91 98000 12345",
                fullName = "Shubham Yadav",
                role = UserRole.VENDOR,
                employeeId = "ST-VND01",
                email = "vendor@shubhamtravel.com",
                assignedClientName = "Corporate Fleet Operations",
                vendorCompany = "Shubham Travel Team"
            )
        )
        dao.insertUsers(users)

        // Seed Historical Shifts for Audit compliance preview
        val now = System.currentTimeMillis()
        val oneDayAgo = now - 86400000L
        val twoDaysAgo = now - (86400000L * 2)

        val shift1 = DutyShift(
            userId = 1L,
            userName = "Rajesh Kumar",
            userPhone = "+91 98765 43210",
            userRole = UserRole.DRIVER,
            mncClientName = "Google Tech Park",
            vehicleRegNumber = "HR 26 CT 9012",
            vehicleModel = "Maruti Suzuki Dzire Tour",
            startTimestamp = twoDaysAgo + 28800000L, // 8:00 AM
            endTimestamp = twoDaysAgo + 64800000L,   // 6:00 PM (10 hrs)
            startOdometerKm = 48070.0,
            endOdometerKm = 48250.0,
            totalKmTraveled = 180.0,
            startPhotoUri = "asset://odometer_sample.jpg",
            endPhotoUri = "asset://odometer_sample.jpg",
            startGpsLocationName = "Sector 39 Unitech Gate 1, Gurgaon",
            startGpsCoordinates = "28.4412° N, 77.0543° E",
            endGpsLocationName = "Sector 39 Hub Parking, Gurgaon",
            endGpsCoordinates = "28.4415° N, 77.0548° E",
            status = ShiftStatus.COMPLETED,
            notes = "Morning employee pickup & evening drop completed without delays."
        )

        val supervisorShift = DutyShift(
            userId = 4L,
            userName = "Amit Singh",
            userPhone = "+91 99888 77665",
            userRole = UserRole.SUPERVISOR,
            mncClientName = "Microsoft IDC & CyberCity Hub",
            vehicleRegNumber = "HUB-DESK-01",
            vehicleModel = "Site Supervisor Station",
            startTimestamp = oneDayAgo + 25200000L, // 7:00 AM
            endTimestamp = oneDayAgo + 61200000L,   // 5:00 PM (10 hrs)
            startOdometerKm = 0.0,
            endOdometerKm = 0.0,
            totalKmTraveled = 0.0,
            startPhotoUri = "asset://supervisor_id.jpg",
            endPhotoUri = "asset://supervisor_id.jpg",
            startGpsLocationName = "Cyber City Building 10 Hub, Gurgaon",
            startGpsCoordinates = "28.4952° N, 77.0891° E",
            endGpsLocationName = "Cyber City Main Dispatch Gate, Gurgaon",
            endGpsCoordinates = "28.4955° N, 77.0894° E",
            status = ShiftStatus.COMPLETED,
            notes = "Shift handover complete. 18 cabs verified on roster, zero defects reported."
        )

        // One currently active driver shift to demonstrate real-time audit roster
        val activeDriverShift = DutyShift(
            userId = 2L,
            userName = "Vikram Sharma",
            userPhone = "+91 98111 22334",
            userRole = UserRole.DRIVER,
            mncClientName = "Microsoft IDC",
            vehicleRegNumber = "DL 01 AB 1234",
            vehicleModel = "Toyota Innova Crysta",
            startTimestamp = now - (3 * 3600000L), // Started 3 hours ago
            endTimestamp = null,
            startOdometerKm = 72320.0,
            endOdometerKm = null,
            totalKmTraveled = 0.0,
            startPhotoUri = "asset://odometer_sample.jpg",
            endPhotoUri = "",
            startGpsLocationName = "Cyber City Tower B Gate 3, Gurgaon",
            startGpsCoordinates = "28.4938° N, 77.0912° E",
            endGpsLocationName = "",
            endGpsCoordinates = "",
            status = ShiftStatus.ACTIVE,
            notes = "Executive transfer roster running"
        )

        dao.insertShift(shift1)
        dao.insertShift(supervisorShift)
        val activeShiftId = dao.insertShift(activeDriverShift)

        // Seed Sample Trips for Shift 1
        dao.insertTrip(
            TripLog(
                shiftId = 1L,
                driverName = "Rajesh Kumar",
                vehicleRegNumber = "HR 26 CT 9012",
                mncClientName = "Google Tech Park",
                tripType = "Employee Morning Pickup",
                routeDescription = "Dwarka Sector 10 -> Unitech Cyber Park",
                passengerCount = 4,
                startOdometer = 48070.0,
                endOdometer = 48115.0,
                distanceKm = 45.0,
                timestamp = twoDaysAgo + 32400000L,
                remarks = "All passengers picked on scheduled time slots"
            )
        )
        dao.insertTrip(
            TripLog(
                shiftId = 1L,
                driverName = "Rajesh Kumar",
                vehicleRegNumber = "HR 26 CT 9012",
                mncClientName = "Google Tech Park",
                tripType = "Inter-Campus Shuttle",
                routeDescription = "Unitech Campus -> Signature Tower -> Unitech",
                passengerCount = 3,
                startOdometer = 48115.0,
                endOdometer = 48145.0,
                distanceKm = 30.0,
                timestamp = twoDaysAgo + 43200000L,
                remarks = "Mid-day team transfer"
            )
        )
        dao.insertTrip(
            TripLog(
                shiftId = 1L,
                driverName = "Rajesh Kumar",
                vehicleRegNumber = "HR 26 CT 9012",
                mncClientName = "Google Tech Park",
                tripType = "Employee Evening Drop",
                routeDescription = "Unitech Cyber Park -> Noida Sector 62",
                passengerCount = 4,
                startOdometer = 48145.0,
                endOdometer = 48250.0,
                distanceKm = 105.0,
                timestamp = twoDaysAgo + 61200000L,
                remarks = "Safe drop confirmation collected from escort"
            )
        )

        // Seed Active Shift Trip
        dao.insertTrip(
            TripLog(
                shiftId = activeShiftId,
                driverName = "Vikram Sharma",
                vehicleRegNumber = "DL 01 AB 1234",
                mncClientName = "Microsoft IDC",
                tripType = "Airport Executive Transfer",
                routeDescription = "IGI Airport Terminal 3 -> Cyber City Tower B",
                passengerCount = 2,
                startOdometer = 72320.0,
                endOdometer = 72355.0,
                distanceKm = 35.0,
                timestamp = now - (2 * 3600000L),
                remarks = "US Client delegate pickup with name board"
            )
        )

        // Seed Fuel Entries
        dao.insertFuelEntry(
            FuelEntry(
                shiftId = 1L,
                driverName = "Rajesh Kumar",
                vehicleRegNumber = "HR 26 CT 9012",
                fuelType = "CNG",
                quantityLiters = 8.5,
                totalAmountInr = 650.0,
                odometerKm = 48115.0,
                fuelStationName = "IGL CNG Station Sector 44",
                timestamp = twoDaysAgo + 36000000L
            )
        )
        dao.insertFuelEntry(
            FuelEntry(
                shiftId = 2L,
                driverName = "Vikram Sharma",
                vehicleRegNumber = "DL 01 AB 1234",
                fuelType = "Diesel",
                quantityLiters = 35.0,
                totalAmountInr = 3150.0,
                odometerKm = 72320.0,
                fuelStationName = "Indian Oil COCO Cyber Hub",
                timestamp = oneDayAgo + 32400000L
            )
        )

        // Seed Maintenance Report
        dao.insertMaintenanceReport(
            MaintenanceReport(
                vehicleRegNumber = "DL 09 CF 4455",
                reporterName = "Amit Singh",
                reporterRole = UserRole.SUPERVISOR,
                issueCategory = "AC Cooling Malfunction",
                priority = "MEDIUM (Attend within 24h)",
                description = "Rear AC vent blower weak cooling reported during afternoon shift. Scheduled for coolant recharge.",
                odometerKm = 61200.0,
                status = "IN_REPAIR",
                timestamp = oneDayAgo + 40000000L
            )
        )
    }
}
