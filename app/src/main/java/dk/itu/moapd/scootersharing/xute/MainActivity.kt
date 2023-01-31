package dk.itu.moapd.scootersharing.xute

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {
    // A set of private constants used in this class .
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    // GUI variables .
    private lateinit var scooterName: EditText
    private lateinit var scooterLocation: EditText
    private lateinit var startRideButton: Button

    private val scooter: Scooter = Scooter("", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Edit texts .
        scooterName = findViewById(R.id.edit_text_name)
        scooterLocation = findViewById(R.id.edit_text_location)

        // Buttons .
        startRideButton = findViewById(R.id.start_ride_button)
        startRideButton.setOnClickListener {
            if (scooterName.text.isNotEmpty() && scooterLocation.text.isNotEmpty()) {
                // Update the object attributes
                val name = scooterName.text.toString().trim()
                val location = scooterLocation.text.toString().trim()
                scooter.setName(name)
                scooter.setLocation(location)

// Reset the text fields and update the UI.
                scooterName.text.clear()
                scooterLocation.text.clear()

                showMessage()
            }
        }


    }

    private fun showMessage() {
// Print a message in the ‘Logcat ‘ system .
        Log.d(TAG, scooter.toString())
    }

}