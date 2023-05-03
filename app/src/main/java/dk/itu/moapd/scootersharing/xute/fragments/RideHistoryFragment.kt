package dk.itu.moapd.scootersharing.xute.fragments

import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.adapters.RealtimeAdapter
import dk.itu.moapd.scootersharing.xute.databinding.FragmentRideHistoryBinding
import dk.itu.moapd.scootersharing.xute.interfaces.ItemClickListener
import dk.itu.moapd.scootersharing.xute.models.Scooter
import dk.itu.moapd.scootersharing.xute.utils.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [RideHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RideHistoryFragment : Fragment(), ItemClickListener {

    // A set of private constants used in this class.
    companion object {
        private lateinit var adapter: RealtimeAdapter
    }

    /**
     * View binding is a feature that allows you to more easily write code that interacts with
     * views. Once view binding is enabled in a module, it generates a binding class for each XML
     * layout file present in that module. An instance of a binding class contains direct references
     * to all views that have an ID in the corresponding layout.
     */
    private lateinit var binding: FragmentRideHistoryBinding

    /**
     * The entry point of the Firebase Authentication SDK.
     */
    private lateinit var auth: FirebaseAuth

    /**
     * A Firebase reference represents a particular location in your Database and can be used for
     * reading or writing data to that Database location.
     */
    private lateinit var database: DatabaseReference

    /**
     * The entry point of the Firebase Storage SDK.
     */
    private lateinit var storage: FirebaseStorage

    /**
     * An extension of `AlertDialog.Builder` to create custom dialogs using a Material theme (e.g.,
     * Theme.MaterialComponents).
     */
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    /**
     * Inflates a custom Android layout used in the input dialog.
     */
    private lateinit var customAlertDialogView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRideHistoryBinding.inflate(
            layoutInflater, container, false
        )

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
        database =
            Firebase.database(DATABASE_URL).reference
        storage = Firebase.storage(BUCKET_URL)
// Create the search query.
        auth.currentUser?.let {
            val query = database
                .child("rideHistory")
                .child(it.uid)
                .orderByChild("timestamp")

            // A class provide by FirebaseUI to make a query in the database to fetch appropriate data.
            val options = FirebaseRecyclerOptions.Builder<Scooter>()
                .setQuery(query, Scooter::class.java)
                .setLifecycleOwner(this)
                .build()

            // Create the custom adapter to bind a list of dummy objects.
            adapter = RealtimeAdapter(requireContext(),this, "RideHistoryUI",options)

            with(binding.contentList) {
                // Define the recycler view layout manager.
                val padding = 2
                val columns = when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> 2
                    else -> 4
                }
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.itemAnimator = null
                recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State
                    ) {
                        outRect.set(padding, padding, padding, padding)
                    }
                })
                recyclerView.adapter = adapter
            }
        }
        // Create a MaterialAlertDialogBuilder instance.
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
// Adding the swipe option.
            val swipeHandler = object : SwipeToDeleteCallback() {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                    super.onSwiped(viewHolder, direction)
                    MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.delete_ride))
                        .setMessage(getString(R.string.alert_supporting_text))
                        .setNeutralButton(getString(R.string.cancel)) { _, _ ->
                            updateList()
                        }.setPositiveButton(getString(R.string.accept)) { _, _ ->
                            adapter.getRef(viewHolder.absoluteAdapterPosition).removeValue()
                            showMessage()
                        }.show()
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(contentList.recyclerView)
        }
    }

    override fun onItemClickListener(scooter: Scooter, position: Int) {
//        TODO 2. update DB: put photo and endRide data in DB (both ridehistory and scooter)
        setFragmentResult("requestKey", bundleOf("data" to scooter.timestamp.toString()))
        findNavController().navigate(R.id.action_rideHistoryFragment_to_cameraFragment)
    }

    override fun onLongItemClickListener(scooter: Scooter, position: Int) {
        // Inflate Custom alert dialog view
        customAlertDialogView = LayoutInflater.from(requireActivity())
            .inflate(R.layout.dialog_add_data, binding.root, false)

        // Launching the custom alert dialog
        launchUpdateAlertDialog(scooter, position)
    }

    /**
     * Building the update alert dialog using the `MaterialAlertDialogBuilder` instance. This method
     * shows a dialog with a single edit text. The user can type a name and add it to the text file
     * dataset or cancel the operation.
     *
     * @param scooter An instance of `Dummy` class.
     */
    private fun launchUpdateAlertDialog(scooter: Scooter, position: Int) {
        // Get the edit text component.
        val editTextLocation = customAlertDialogView
            .findViewById<TextInputEditText>(R.id.edit_text_name)
        editTextLocation?.setText(scooter.location)

        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setTitle(getString(R.string.dialog_update_title))
            .setMessage("You are updating ${scooter.name}'s location")
            .setPositiveButton(getString(R.string.update_button)) { dialog, _ ->
                val location = editTextLocation?.text.toString()
                if (location.isNotEmpty()) {
                    scooter.location = location
                    scooter.timestamp = System.currentTimeMillis()
                    adapter.getRef(position).setValue(scooter)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateList() {
        with(binding.contentList) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
        }
    }

    /** Print a message in the ‘Logcat ‘ system and show snackbar message at bottom of user screen.
     */
//    private fun showMessage(scooter: Scooter) {
//        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//        imm?.hideSoftInputFromWindow(binding.updateRideButton.windowToken, 0)
//        val snackbar =
//            Snackbar.make(
//                binding.updateRideButton,
//                scooter.customMessage("updated"),
//                Snackbar.LENGTH_LONG
//            )
//        snackbar.show()
//    }

    private fun showMessage() {
        Log.d(TAG(), getString(R.string.started))
        val snackbar = Snackbar.make(
            binding.root, getString(R.string.ride_deleted), Snackbar.LENGTH_LONG
        )
        snackbar.show()
    }
}