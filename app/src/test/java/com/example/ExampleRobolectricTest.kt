package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.DutyShift
import com.example.data.model.ShiftStatus
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.repository.TransportRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Shubham Travel branding`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Shubham Travel", appName)
    }

    @Test
    fun `database and repository start driver shift and verify csv output`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repo = TransportRepository(db.shubhamDao())

        val testUser = UserAccount(
            phoneNumber = "+91 99999 88888",
            fullName = "Test Driver",
            role = UserRole.DRIVER,
            employeeId = "ST-D999"
        )
        val userId = repo.registerUser(testUser)
        assertTrue(userId > 0)

        val shiftId = repo.startDriverShift(
            userId = userId,
            userName = testUser.fullName,
            userPhone = testUser.phoneNumber,
            mncClientName = "Google Tech Park",
            vehicleReg = "DL 01 AB 9999",
            vehicleModel = "Maruti Dzire",
            startOdometer = 10000.0,
            photoUri = "/test/photo.jpg",
            gpsLocationName = "Cyber Hub Gate 1",
            gpsCoordinates = "28.4952° N, 77.0891° E",
            notes = "Shift started cleanly"
        )
        assertTrue(shiftId > 0)

        val active = repo.getActiveShift(userId).first()
        assertNotNull(active)
        assertEquals("Google Tech Park", active?.mncClientName)
        assertEquals(ShiftStatus.ACTIVE, active?.status)

        // End shift
        repo.endDriverShift(
            shift = active!!,
            endOdometer = 10050.0,
            photoUri = "/test/end_photo.jpg",
            gpsLocationName = "Cyber Hub Gate 1",
            gpsCoordinates = "28.4952° N, 77.0891° E"
        )

        val csv = repo.formatAuditCsv(listOf(active.copy(endOdometerKm = 10050.0, totalKmTraveled = 50.0, status = ShiftStatus.COMPLETED)))
        assertTrue(csv.contains("Test Driver"))
        assertTrue(csv.contains("Google Tech Park"))
    }

    @Test
    fun `verify vendor user registration and role`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repo = TransportRepository(db.shubhamDao())

        val vendorUser = UserAccount(
            phoneNumber = "+91 98000 12345",
            fullName = "Shubham Yadav",
            role = UserRole.VENDOR,
            employeeId = "ST-VND01",
            email = "vendor@shubhamtravel.com",
            vendorCompany = "Shubham Travel Team"
        )
        val id = repo.registerUser(vendorUser)
        assertTrue(id > 0)

        val retrieved = repo.getUserById(id)
        assertNotNull(retrieved)
        assertEquals(UserRole.VENDOR, retrieved?.role)
        assertEquals("vendor@shubhamtravel.com", retrieved?.email)
    }
}
