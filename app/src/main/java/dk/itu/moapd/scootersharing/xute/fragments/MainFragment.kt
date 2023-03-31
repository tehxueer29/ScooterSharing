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

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.interfaces.ItemClickListener
import dk.itu.moapd.scootersharing.xute.adapters.RealtimeAdapter
import dk.itu.moapd.scootersharing.xute.utils.SwipeToDeleteCallback
import dk.itu.moapd.scootersharing.xute.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.xute.models.Image
import dk.itu.moapd.scootersharing.xute.models.Scooter
import dk.itu.moapd.scootersharing.xute.utils.BUCKET_URL
import dk.itu.moapd.scootersharing.xute.utils.DATABASE_URL
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    // A set of private constants used in this class.
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

//    /**
//     * This object launches a new activity and receives back some result data.
//     */
//    private val galleryLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        galleryResult(result)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(
            layoutInflater, container, false
        )

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
//        database =
//            Firebase.database(DATABASE_URL).reference
//        storage = Firebase.storage(BUCKET_URL)
//
//        // Define the add button behavior.
//        binding.floatingActionButton.setOnClickListener {
//            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
//                type = "image/*"
//            }
//            galleryLauncher.launch(galleryIntent)
//        }
//
//        // Create the search query.
//        auth.currentUser?.let {
//            val query = database
//                .child("rideHistory")
//                .child(it.uid)
//                .orderByChild("timestamp")
//
//            // A class provide by FirebaseUI to make a query in the database to fetch appropriate data.
//            val options = FirebaseRecyclerOptions.Builder<Scooter>()
//                .setQuery(query, Scooter::class.java)
//                .setLifecycleOwner(this)
//                .build()
//
//            // Create the custom adapter to bind a list of dummy objects.
//            adapter = RealtimeAdapter(this, options)
//
//            with(binding.contentList) {
//                // Define the recycler view layout manager.
//                val padding = 2
//                val columns = when (resources.configuration.orientation) {
//                    Configuration.ORIENTATION_PORTRAIT -> 2
//                    else -> 4
//                }
//                recyclerView.layoutManager = LinearLayoutManager(context)
//                recyclerView.itemAnimator = null
//                recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
//                    override fun getItemOffsets(
//                        outRect: Rect,
//                        view: View,
//                        parent: RecyclerView,
//                        state: RecyclerView.State
//                    ) {
//                        outRect.set(padding, padding, padding, padding)
//                    }
//                })
//                recyclerView.adapter = adapter
//            }
//        }
//        // Create a MaterialAlertDialogBuilder instance.
//        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireActivity())

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
            rideHistoryButton.setOnClickListener {
//                updateList(view)
                findNavController().navigate(R.id.action_mainFragment_to_updateRideFragment2)
            }
//
//            listRideButton.setOnClickListener {
//                // Define the list view adapter.
//                updateList()
//            }

            signOutButton.setOnClickListener {
                Log.d(TAG, "signing out")
//                sign out from firebase
                auth.signOut()

//                sign out from google
                val googleSignInOptions =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                val googleSignInClient =
                    GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
                googleSignInClient.signOut()

                startLoginFragment()
            }



        }

    }

//    private fun updateList() {
//        with(binding.contentList) {
//            recyclerView.layoutManager = LinearLayoutManager(context)
//            recyclerView.adapter = adapter
//        }
//    }

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
//
//    /**
//     * This method will be executed when the user press an item in the `RecyclerView` for a long
//     * time.
//     *
//     * @param scooter An instance of `Scooter` class.
//     * @param position The selected position in the `RecyclerView`.
//     */
//    override fun onItemClickListener(scooter: Scooter, position: Int) {
//        // Inflate Custom alert dialog view
//        customAlertDialogView = LayoutInflater.from(requireActivity())
//            .inflate(R.layout.dialog_add_data, binding.root, false)
//
//        // Launching the custom alert dialog
//        launchUpdateAlertDialog(scooter, position)
//    }

//    /**
//     * Building the update alert dialog using the `MaterialAlertDialogBuilder` instance. This method
//     * shows a dialog with a single edit text. The user can type a name and add it to the text file
//     * dataset or cancel the operation.
//     *
//     * @param scooter An instance of `Dummy` class.
//     */
//    private fun launchUpdateAlertDialog(scooter: Scooter, position: Int) {
//        // Get the edit text component.
//        val editTextLocation = customAlertDialogView
//            .findViewById<TextInputEditText>(R.id.edit_text_name)
//        editTextLocation?.setText(scooter.location)
//
//        materialAlertDialogBuilder.setView(customAlertDialogView)
//            .setTitle(getString(R.string.dialog_update_title))
//            .setMessage("You are updating ${scooter.name}'s location")
//            .setPositiveButton(getString(R.string.update_button)) { dialog, _ ->
//                val location = editTextLocation?.text.toString()
//                if (location.isNotEmpty()) {
//                    scooter.location = location
//                    scooter.timestamp = System.currentTimeMillis()
//                    adapter.getRef(position).setValue(scooter)
//                }
//                dialog.dismiss()
//            }
//            .setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
//                dialog.dismiss()
//            }
//            .show()
//    }
//
//    /**
//     * When the second activity finishes (i.e., the photo gallery intent), it returns a result to
//     * this activity. If the user selects an image correctly, we can get a reference of the selected
//     * image and send it to the Firebase Storage.
//     *
//     * @param result A container for an activity result as obtained form `onActivityResult()`.
//     */
//    private fun galleryResult(result: ActivityResult) {
//        if (result.resultCode == AppCompatActivity.RESULT_OK) {
//            // Create the folder structure save the selected image in the bucket.
//            auth.currentUser?.let {
//                val filename = UUID.randomUUID().toString()
//                val image = storage.reference.child("images/${it.uid}/$filename")
//                val thumbnail = storage.reference.child("images/${it.uid}/${filename}_thumbnail")
////            Log.d("ok123", thumbnail.toString())
//                result.data?.data?.let { uri ->
//                    uploadImageToBucket(uri, image, thumbnail)
//                }
//            }
//        }
//    }
//
//    /**
//     * This method uploads the original and the thumbnail images to the Firebase Storage, and
//     * creates a reference of uploaded images in the database.
//     *
//     * @param uri The URI of original image.
//     * @param image The original image's storage reference in the Firebase Storage.
//     * @param thumbnail The thumbnail image's storage reference in the Firebase Storage.
//     */
//    private fun uploadImageToBucket(
//        uri: Uri,
//        image: StorageReference,
//        thumbnail: StorageReference
//    ) {
//        // Code for showing progress bar while uploading.
//        binding.contentList.progressBar.visibility = View.VISIBLE
//
//        // Upload the original image.
//        image.putFile(uri).addOnSuccessListener { imageUrl ->
//
//            // Upload the thumbnail image.
//            thumbnail.putFile(createThumbnail(uri)).addOnSuccessListener {
//
//                // Save the image reference in the database.
//                imageUrl.metadata?.reference?.downloadUrl?.addOnSuccessListener { imageUri ->
//                    saveImageInDatabase(imageUri.toString(), image.path)
//                    binding.contentList.progressBar.visibility = View.GONE
//                }
//            }
//        }
//    }
//
//    /**
//     * This method creates a squared thumbnail of the uploaded image. We are going to use the
//     * thumbnail to show the images into the `RecyclerView`.
//     *
//     * @param uri The immutable URI reference of uploaded image.
//     * @param size The image resolution used to create the thumbnail (Default: 300).
//     *
//     * @return The immutable URI reference of created thumbnail image.
//     */
//    private fun createThumbnail(uri: Uri, size: Int = 300): Uri {
//        val decode =
//            BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(uri))
//        val thumbnail = ThumbnailUtils.extractThumbnail(
//            decode, size, size, ThumbnailUtils.OPTIONS_RECYCLE_INPUT
//        )
//        return getImageUri(thumbnail)
//    }
//
//    /**
//     * This method saves the bitmap in the temporary folder and return its immutable URI reference.
//     *
//     * @param image The thumbnail bitmap created in memory.
//     *
//     * @return The immutable URI reference of created thumbnail image.
//     */
//    private fun getImageUri(image: Bitmap): Uri {
//        val file = File(requireActivity().cacheDir, "thumbnail")
//        val outStream = FileOutputStream(file)
//        image.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
//        outStream.close()
//        return Uri.fromFile(file)
//    }
//
//    /**
//     * This method saves a reference of uploaded image in the database. The Firebase Storage does
//     * NOT have a option to observe changes in the bucket an automatically updates the application.
//     * We must use a database to have this feature in our application.
//     *
//     * @param url The public URL of uploaded image.
//     * @param path The private URL of uploaded image on Firebase Storage.
//     */
//    private fun saveImageInDatabase(url: String, path: String) {
//        val timestamp = System.currentTimeMillis()
//        val image = Image(url, path, timestamp)
//
//        // In the case of authenticated user, create a new unique key for the object in the
//        // database.
//        auth.currentUser?.let { user ->
//            val uid = database.child("images")
//                .child(user.uid)
//                .push()
//                .key
//
//            // Insert the object in the database.
//            uid?.let {
//                database.child("images")
//                    .child(user.uid)
//                    .child(it)
//                    .setValue(image)
//            }
//        }
//    }

//    /**
//     * This method deletes a reference of uploaded image in the database, and the original and
//     * thumbnail images from the Firebase Storage.
//     *
//     * @param image An instance of `Image` class.
//     * @param position The image position in the `RecyclerView`.
//     */
//    private fun deleteImage(image: Image, position: Int) {
//        // Remove an item from the Firebase Realtime database.
//        adapter.getRef(position).removeValue().addOnSuccessListener {
//
//            // Remove the thumbnail image.
//            storage.reference.child("${image.path}_thumbnail")
//                .delete().addOnSuccessListener {
//
//                    // Remove the original image.
//                    storage.reference.child("${image.path}")
//                        .delete().addOnSuccessListener {
////                            snackBar("Item deleted successfully")
//                        }
//                }
//        }
//    }

//    /**
//     * Make a standard snack-bar that just contains text.
//     */
//    private fun snackBar(
//        text: CharSequence,
//        duration: Int = Snackbar.LENGTH_SHORT
//    ) {
//        Snackbar
//            .make(findViewById(android.R.id.content), text, duration)
//            .show()
//    }

}