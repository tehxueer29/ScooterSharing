/**MIT License

Copyright (c) 2023 Teh Xue Er

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package dk.itu.moapd.scootersharing.xute.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.adapters.ItemClickListener
import dk.itu.moapd.scootersharing.xute.adapters.RealtimeAdapter
import dk.itu.moapd.scootersharing.xute.adapters.SwipeToDeleteCallback
import dk.itu.moapd.scootersharing.xute.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.xute.models.Scooter

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment(), ItemClickListener {

    // A set of private constants used in this class .
    companion object {
        private val TAG = MainFragment::class.qualifiedName
        private lateinit var adapter: RealtimeAdapter
    }

    /**
     * View binding is a feature that allows you to more easily write code that interacts with
     * views. Once view binding is enabled in a module, it generates a binding class for each XML
     * layout file present in that module. An instance of a binding class contains direct references
     * to all views that have an ID in the corresponding layout.
     */
    private lateinit var binding: FragmentMainBinding

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(
            layoutInflater, container, false
        )

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
        database = Firebase.database("https://moapd-2023-478a9-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Create the search query.
        auth.currentUser?.let {
            val query = database
                .child("scooter")

            // A class provide by FirebaseUI to make a query in the database to fetch appropriate data.
            val options = FirebaseRecyclerOptions.Builder<Scooter>()
                .setQuery(query, Scooter::class.java)
                .setLifecycleOwner(this)
                .build()

            // Create the custom adapter to bind a list of dummy objects.
            adapter = RealtimeAdapter(this, options)

            with(binding.contentList) {
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = adapter
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, auth.currentUser.toString() + "CHECKING")

        // Check if the user is not logged and redirect her/him to the LoginActivity.
        if (auth.currentUser == null) startLoginFragment()

        // Set the user information.
        val user = auth.currentUser
        if (user != null) {
            binding.description.text = getString(
                R.string.firebase_user_description,
                user.email
            )
        }

        // makes all variables start with [mainBinding.]
        with(binding) {
//            // The start ride button listener.
            startRideButton.setOnClickListener {
//                updateList(view)
                findNavController().navigate(R.id.action_mainFragment_to_startRideFragment2)
            }
//
            updateRideButton.setOnClickListener {
//                updateList(view)
                findNavController().navigate(R.id.action_mainFragment_to_updateRideFragment2)
            }
//
            listRideButton.setOnClickListener {
                // Define the list view adapter.
                updateList(view)
            }

            signOutButton.setOnClickListener {
                Log.d(TAG, "signing out")
                auth.signOut()
                Log.d(TAG, auth.currentUser.toString()) //null
                startLoginFragment()
                Log.d(TAG, auth.currentUser.toString()) //null

            }

//            adapter.setOnItemClickListener {
//                //here you have your UserModel in your fragment, do whatever you want to with it
//                Log.d(TAG, "clicked!MAINFRAG")
//
//                MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.delete_ride))
//                    .setMessage(getString(R.string.alert_supporting_text))
//                    .setNeutralButton(getString(R.string.cancel)) { _, _ ->
//                    }.setPositiveButton(getString(R.string.accept)) { _, _ ->
////                        ridesDB.deleteScooter(it.timestamp)
////                        adapter.getRef("scooter/timestamp").removeValue()
//                        Log.d(TAG, it.timestamp.toString())
//                        database.child("scooter/${it.timestamp}").removeValue()
//                        showMessage()
//                    }.show()
//            }

            // Adding the swipe option.
            val swipeHandler = object : SwipeToDeleteCallback() {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    super.onSwiped(viewHolder, direction)
                    adapter.getRef(viewHolder.absoluteAdapterPosition).removeValue()
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(contentList.recyclerView)

        }

    }

    private fun updateList(view: View) {
        with(binding.contentList) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
        }
    }

    private fun showMessage() {
        Log.d(TAG, getString(R.string.started))
        val snackbar = Snackbar.make(
            binding.startRideButton, getString(R.string.ride_deleted), Snackbar.LENGTH_LONG
        )
        snackbar.show()
    }

    private fun startLoginFragment() {
        findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
    }

    override fun onItemClickListener(scooter: Scooter, position: Int) {
        fun onItemClickListener(scooter: Scooter, position: Int) {}
    }

}