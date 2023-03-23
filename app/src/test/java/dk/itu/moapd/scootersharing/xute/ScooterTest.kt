package dk.itu.moapd.scootersharing.xute

import dk.itu.moapd.scootersharing.xute.models.Scooter
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ScooterTest {
    // Create two instances of the Scooter class to compare
    private val scooter = Scooter("CPH001", "ITU", 1648067996177)
    private val scooter2 = Scooter("CPH001", "ITU", 1648067996177)

    @Test
    fun scooter_CreateObject_ReturnsObject() {
        assertEquals(scooter, scooter2)
    }

    @Test
    fun scooter_CreateObject_ReturnsFalseObject() {
        val scooter3 = Scooter("CPH002", "ITU", 1648067996177)

        // Check that the two instances are not equal (CPH001 vs CPH002)
        assertNotEquals(scooter, scooter3)
    }

    @Test
    fun scooter_GetTime_ReturnsTime() {
        assertEquals(scooter.getTimestampToString(), "23/03/2022 09:39")
    }

    @Test
    fun scooter_GetCustomMessage_ReturnsCustomMessage() {
        assertEquals(
            scooter.customMessage("started"),
            "Ride started using Scooter(name=CPH001, location=ITU)."
        )
    }
}