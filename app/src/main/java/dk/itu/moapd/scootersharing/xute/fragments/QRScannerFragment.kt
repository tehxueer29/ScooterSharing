package dk.itu.moapd.scootersharing.xute.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.models.Scooter
import dk.itu.moapd.scootersharing.xute.utils.DATABASE_URL
import dk.itu.moapd.scootersharing.xute.utils.TAG

/**
 * A simple [Fragment] subclass.
 * Use the [QRScannerFragment] factory method to
 * create an instance of this fragment.
 */
class QRScannerFragment : Fragment() {
    /**
     * The entry point of the Firebase Authentication SDK.
     */
    private lateinit var auth: FirebaseAuth

    /**
     * A Firebase reference represents a particular location in your Database and can be used for
     * reading or writing data to that Database location.
     */
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
        database = Firebase.database(DATABASE_URL).reference
        Log.d(TAG(), "start?")
        startQrCode()

    }

    private fun startQrCode() {

        val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC
        ).build()

        val scanner = GmsBarcodeScanning.getClient(requireContext(), options)

        scanner.startScan().addOnSuccessListener { barcode ->
            // Task completed successfully
            val scooterName: String? = barcode.rawValue
            Log.d(TAG(), scooterName.toString())
            val message = "You have started $scooterName!"

            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.start_ride))
                .setMessage(message).setPositiveButton(getString(R.string.accept)) { _, _ ->
                    auth.currentUser?.let { user ->
                        val query =
                            database.child("scooter").orderByChild("name").equalTo(scooterName)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Do something here
                                    Log.d(TAG(), dataSnapshot.value.toString())

                                    val data = dataSnapshot.value as Map<*, *>
                                    val result = data.values.first() as Map<*, *>

                                    val scooter = Scooter(
                                        result["name"] as String?,
                                        result["location"] as String?
                                    )
//                                    Log.i(TAG(), scooter.toString())
                                    // Handle retrieved data here
                                    val uid =
                                        database.child("rideHistory").child(user.uid).push().key

                                    // Insert the object in the database.
                                    uid?.let {
                                        database.child("rideHistory").child(user.uid).child(it)
                                            .setValue(scooter)
                                    }

                                } else {
                                    // Child with name "cph00x" does not exist in the list of scooters
                                    // Do something here
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Failed to read value
                                Log.w(
                                    TAG(), "Failed to read value.", databaseError.toException()
                                )
                            }
                        })
                    }
                    findNavController().navigate(R.id.action_QRScannerFragment_to_rideHistoryFragment)
                }.show()
        }.addOnCanceledListener {
            // Task canceled
        }.addOnFailureListener { e ->
            // Task failed with an exception
            Log.e(TAG(), e.toString())

        }
    }
}