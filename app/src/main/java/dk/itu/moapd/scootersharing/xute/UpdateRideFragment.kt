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
import dk.itu.moapd.scootersharing.xute.databinding.FragmentUpdateRideBinding

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

    // A set of private constants used in this class .
    companion object {
        private val TAG = UpdateRideFragment::class.qualifiedName
        lateinit var ridesDB: RidesDB
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpdateRideBinding.inflate(
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
            scooterName.hint = ridesDB.getCurrentScooter().name

            // The update ride button listener.
            updateRideButton.setOnClickListener {

                if (scooterLocation.text.isNotEmpty()) {
                    // Update the object attributes
                    val location = scooterLocation.text.toString().trim()
                    ridesDB.updateCurrentScooter(location)

                    // Reset the text fields and update the UI.
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
        imm?.hideSoftInputFromWindow(binding.updateRideButton.windowToken, 0)
        Log.d(TAG, ridesDB.getCurrentScooter().toString())
        val snackbar =
            Snackbar.make(
                binding.updateRideButton,
                ridesDB.getCurrentScooter().toString(),
                Snackbar.LENGTH_LONG
            )
        snackbar.show()
    }

}