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
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.databinding.FragmentUpdateRideBinding
import dk.itu.moapd.scootersharing.xute.models.Scooter
import dk.itu.moapd.scootersharing.xute.utils.DATABASE_URL
import dk.itu.moapd.scootersharing.xute.utils.TAG

/**
 * A simple [Fragment] subclass.
 * Use the [UpdateRideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateRideFragment : Fragment() {
    /**
     * View binding is a feature that allows you to more easily write code that interacts with
     * views. Once view binding is enabled in a module, it generates a binding class for each XML
     * layout file present in that module. An instance of a binding class contains direct references
     * to all views that have an ID in the corresponding layout.
     */
    private lateinit var binding: FragmentUpdateRideBinding

    /**
     * The entry point of the Firebase Authentication SDK.
     */
    private lateinit var auth: FirebaseAuth

    /**
     * A Firebase reference represents a particular location in your Database and can be used for
     * reading or writing data to that Database location.
     */
    private lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpdateRideBinding.inflate(
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
//            scooterName.hint = ridesDB.getCurrentScooter().name
            auth.currentUser?.let { it ->
                database
                    .child("scooter")
                    .child(it.uid)
                    .orderByChild("timestamp")
                    .limitToLast(1)
                    .get()
                    .addOnSuccessListener {
                        Log.i("firebase", "Got value ${it.getValue<Scooter>()}")
                        Log.i("firebase", "Got value ${it.value}")
//                        it.getValue<Scooter>()
//                        val scooter = Scooter(it.value.key)
//                        scooterName.hint = it.value

                    }.addOnFailureListener {
                        Log.e("firebase", "Error getting data", it)
                    }
            }


            // The update ride button listener.
            updateRideButton.setOnClickListener {

                if (scooterLocation.text.isNotEmpty()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.update_ride))
                        .setMessage(getString(R.string.alert_supporting_text))
                        .setNeutralButton(getString(R.string.cancel)) { _, _ ->
                        }
                        .setPositiveButton(getString(R.string.accept)) { _, _ ->
                            // Update the object attributes
                            val location = scooterLocation.text.toString().trim()
//                            TODO
//                            ridesDB.updateCurrentScooter(location)

                            // Reset the text fields and update the UI.
                            scooterLocation.text.clear()

//                            TODO
//                            showMessage(scooter)
                        }
                        .show()
                }
            }
        }
    }

    /** Print a message in the ‘Logcat ‘ system and show snackbar message at bottom of user screen.
     */
    private fun showMessage(scooter: Scooter) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.updateRideButton.windowToken, 0)
        val snackbar =
            Snackbar.make(
                binding.updateRideButton,
                scooter.customMessage("updated"),
                Snackbar.LENGTH_LONG
            )
        snackbar.show()
    }

}