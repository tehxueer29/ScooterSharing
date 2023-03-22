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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.models.RidesDB
import dk.itu.moapd.scootersharing.xute.adapters.CustomArrayAdapter
import dk.itu.moapd.scootersharing.xute.databinding.FragmentMainBinding

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    // A set of private constants used in this class .
    companion object {
        private val TAG = MainFragment::class.qualifiedName
        lateinit var ridesDB: RidesDB
        private lateinit var adapter: CustomArrayAdapter
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(
            layoutInflater, container, false
        )

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, auth.currentUser.toString() + "CHECKING")

        // Check if the user is not logged and redirect her/him to the LoginActivity.
        if (auth.currentUser == null) startLoginFragment()

        // Set the user information.
        val user = auth.currentUser
        binding.description.text = getString(
            R.string.firebase_user_description,
            if (user?.email!!.isEmpty()) user.phoneNumber else user.email
        )

        // Singleton to share an object between the app activities .
        ridesDB = RidesDB.get(requireContext())

//        get all rides object
        val data = ridesDB.getRidesList()

        // Create the custom adapter to populate a list of dummy objects.
        adapter = CustomArrayAdapter(data)

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

            adapter.setOnItemClickListener {
                //here you have your UserModel in your fragment, do whatever you want to with it
                Log.d(TAG, "clicked!MAINFRAG")

                MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.delete_ride))
                    .setMessage(getString(R.string.alert_supporting_text))
                    .setNeutralButton(getString(R.string.cancel)) { _, _ ->
                    }.setPositiveButton(getString(R.string.accept)) { _, _ ->
                        ridesDB.deleteScooter(it.timestamp)
                        // Define the list view adapter.
                        updateList(view)
                        showMessage()
                    }.show()
            }

        }

    }

    private fun updateList(view: View) {
        with(binding) {
            contentList.recyclerView.layoutManager = LinearLayoutManager(context)
            contentList.recyclerView.adapter = adapter
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
}