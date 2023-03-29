package dk.itu.moapd.scootersharing.xute.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.models.RidesDB
import dk.itu.moapd.scootersharing.xute.databinding.FragmentStartRideBinding
import dk.itu.moapd.scootersharing.xute.models.Scooter
import dk.itu.moapd.scootersharing.xute.utils.DATABASE_URL

/**
 * A simple [Fragment] subclass.
 * Use the [StartRideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StartRideFragment : Fragment() {
    private lateinit var binding: FragmentStartRideBinding

    /**
     * The entry point of the Firebase Authentication SDK.
     */
    private lateinit var auth: FirebaseAuth


    /**
     * A Firebase reference represents a particular location in your Database and can be used for
     * reading or writing data to that Database location.
     */
    private lateinit var database: DatabaseReference

    // A set of private constants used in this class .
    companion object {
        private val TAG = StartRideFragment::class.qualifiedName
//        lateinit var ridesDB: RidesDB
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStartRideBinding.inflate(
            layoutInflater, container, false
        )
        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
        database =
            Firebase.database(DATABASE_URL).reference

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            // The start ride button listener.
            startRideButton.setOnClickListener {
                if (scooterName.text.isNotEmpty() && scooterLocation.text.isNotEmpty()) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.start_ride))
                        .setMessage(getString(R.string.alert_supporting_text))
                        .setNeutralButton(getString(R.string.cancel)) { _, _ ->
                        }
                        .setPositiveButton(getString(R.string.accept)) { _, _ ->
                            // Update the object attributes
                            val name = scooterName.text.toString().trim()
                            val location = scooterLocation.text.toString().trim()

                            // In the case of authenticated user, create a new unique key for the object in
                            // the database.
                            auth.currentUser?.let { user ->
                                val uid = database.child("scooter")
                                    .child(user.uid)
                                    .push()
                                    .key

                                // Insert the object in the database.
                                uid?.let {
                                    database.child("scooter")
                                        .child(user.uid)
                                        .child(it)
                                        .setValue(Scooter(name, location))
                                }
                            }

                            // Reset the text fields and update the UI.
                            scooterName.text.clear()
                            scooterLocation.text.clear()

                            showMessage()
                        }
                        .show()

                }
            }
        }
    }

    /** Print a message in the ‘Logcat ‘ system and show snackbar message at bottom of user screen.
     */
    private fun showMessage() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.startRideButton.windowToken, 0)
        val snackbar =
            Snackbar.make(
                binding.startRideButton,
                "Ride Started",
                Snackbar.LENGTH_LONG
            )
        snackbar.show()
    }

}