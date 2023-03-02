package dk.itu.moapd.scootersharing.xute

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.scootersharing.xute.databinding.FragmentStartRideBinding

/**
 * A simple [Fragment] subclass.
 * Use the [StartRideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StartRideFragment : Fragment() {
    private lateinit var binding: FragmentStartRideBinding

    // A set of private constants used in this class .
    companion object {
        private val TAG = StartRideFragment::class.qualifiedName
        lateinit var ridesDB: RidesDB
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStartRideBinding.inflate(
            layoutInflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Singleton to share an object between the app activities .
        ridesDB = RidesDB.get(requireContext())

//        get all rides object
        val data = ridesDB.getRidesList()

        with(binding) {
            // The start ride button listener.
            startRideButton.setOnClickListener {
                if (scooterName.text.isNotEmpty() && scooterLocation.text.isNotEmpty()) {
                    // Update the object attributes
                    val name = scooterName.text.toString().trim()
                    val location = scooterLocation.text.toString().trim()
                    ridesDB.addScooter(name, location)

                    // Reset the text fields and update the UI.
                    scooterName.text.clear()
                    scooterLocation.text.clear()

                    showMessage()
                }
            }
        }
    }
    /** Print a message in the ‘Logcat ‘ system and show snackbar message at bottom of user screen.
     */
    private fun showMessage() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.startRideButton.windowToken, 0)
        Log.d(TAG, ridesDB.getCurrentScooter().toString())
        val snackbar =
            Snackbar.make(binding.startRideButton, ridesDB.getCurrentScooter().toString(), Snackbar.LENGTH_LONG)
        snackbar.show()
    }
}