package dk.itu.moapd.scootersharing.xute.fragments

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.adapters.RealtimeAdapter
import dk.itu.moapd.scootersharing.xute.databinding.FragmentStartRideBinding
import dk.itu.moapd.scootersharing.xute.interfaces.ItemClickListener
import dk.itu.moapd.scootersharing.xute.models.Image
import dk.itu.moapd.scootersharing.xute.models.Scooter
import dk.itu.moapd.scootersharing.xute.utils.BUCKET_URL
import dk.itu.moapd.scootersharing.xute.utils.DATABASE_URL
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [StartRideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StartRideFragment : Fragment(), ItemClickListener {

    // A set of private constants used in this class .
    companion object {
        private lateinit var adapter: RealtimeAdapter
    }

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

    /**
     * The entry point of the Firebase Storage SDK.
     */
    private lateinit var storage: FirebaseStorage

    /**
     * This object launches a new activity and receives back some result data.
     */
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        galleryResult(result)
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
        storage = Firebase.storage(BUCKET_URL)
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
            adapter = RealtimeAdapter(this, "StartRideUI", options)

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
//            Show all rides in maps listener
            viewMapsButton.setOnClickListener {
                findNavController().navigate(R.id.action_startRideFragment_to_mapsFragment)
            }

            // The start ride button listener.
//            startRideButton.setOnClickListener {
//                if (scooterName.text.isNotEmpty() && scooterLocation.text.isNotEmpty()) {
//
//                    MaterialAlertDialogBuilder(requireContext())
//                        .setTitle(getString(R.string.start_ride))
//                        .setMessage(getString(R.string.alert_supporting_text))
//                        .setNeutralButton(getString(R.string.cancel)) { _, _ ->
//                        }
//                        .setPositiveButton(getString(R.string.accept)) { _, _ ->
//                            // Update the object attributes
//                            val name = scooterName.text.toString().trim()
//                            val location = scooterLocation.text.toString().trim()
//
//                            val scooter = Scooter(name, location)
//
//                            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
//                                type = "image/*"
//                            }
//                            galleryLauncher.launch(galleryIntent)
//
//                            // In the case of authenticated user, create a new unique key for the object in
//                            // the database.
//                            auth.currentUser?.let { user ->
//                                val uid = database.child("rideHistory")
//                                    .child(user.uid)
//                                    .push()
//                                    .key
//
//                                // Insert the object in the database.
//                                uid?.let {
//                                    database.child("rideHistory")
//                                        .child(user.uid)
//                                        .child(it)
//                                        .setValue(scooter)
//                                }
//                            }
//
//                            // Reset the text fields and update the UI.
//                            scooterName.text.clear()
//                            scooterLocation.text.clear()
//
//                            showMessage(scooter)
//                        }
//                        .show()
//
//                }
//            }
        }
    }

    /** Print a message in the ‘Logcat ‘ system and show snackbar message at bottom of user screen.
     */
//    private fun showMessage(scooter: Scooter) {
//        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//        imm?.hideSoftInputFromWindow(binding.startRideButton.windowToken, 0)
//        val snackbar =
//            Snackbar.make(
//                binding.startRideButton,
//                scooter.customMessage("started"),
//                Snackbar.LENGTH_LONG
//            )
//        snackbar.show()
//    }

    /**
     * When the second activity finishes (i.e., the photo gallery intent), it returns a result to
     * this activity. If the user selects an image correctly, we can get a reference of the selected
     * image and send it to the Firebase Storage.
     *
     * @param result A container for an activity result as obtained form `onActivityResult()`.
     */
    private fun galleryResult(result: ActivityResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Create the folder structure save the selected image in the bucket.
            auth.currentUser?.let {
                val filename = UUID.randomUUID().toString()
                val image = storage.reference.child("images/$filename")
                val thumbnail = storage.reference.child("images/${filename}_thumbnail")
//            Log.d("ok123", thumbnail.toString())
                result.data?.data?.let { uri ->
                    uploadImageToBucket(uri, image, thumbnail)
                }
            }
        }
    }

    /**
     * This method uploads the original and the thumbnail images to the Firebase Storage, and
     * creates a reference of uploaded images in the database.
     *
     * @param uri The URI of original image.
     * @param image The original image's storage reference in the Firebase Storage.
     * @param thumbnail The thumbnail image's storage reference in the Firebase Storage.
     */
    private fun uploadImageToBucket(
        uri: Uri,
        image: StorageReference,
        thumbnail: StorageReference
    ) {
        // Code for showing progress bar while uploading.
//        binding.contentList.progressBar.visibility = View.VISIBLE

        // Upload the original image.
        image.putFile(uri).addOnSuccessListener { imageUrl ->

            // Upload the thumbnail image.
            thumbnail.putFile(createThumbnail(uri)).addOnSuccessListener {

                // Save the image reference in the database.
                imageUrl.metadata?.reference?.downloadUrl?.addOnSuccessListener { imageUri ->
                    saveImageInDatabase(imageUri.toString(), image.path)
//                    binding.contentList.progressBar.visibility = View.GONE
                }
            }
        }
    }

    /**
     * This method creates a squared thumbnail of the uploaded image. We are going to use the
     * thumbnail to show the images into the `RecyclerView`.
     *
     * @param uri The immutable URI reference of uploaded image.
     * @param size The image resolution used to create the thumbnail (Default: 300).
     *
     * @return The immutable URI reference of created thumbnail image.
     */
    private fun createThumbnail(uri: Uri, size: Int = 300): Uri {
        val decode =
            BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(uri))
        val thumbnail = ThumbnailUtils.extractThumbnail(
            decode, size, size, ThumbnailUtils.OPTIONS_RECYCLE_INPUT
        )
        return getImageUri(thumbnail)
    }

    /**
     * This method saves the bitmap in the temporary folder and return its immutable URI reference.
     *
     * @param image The thumbnail bitmap created in memory.
     *
     * @return The immutable URI reference of created thumbnail image.
     */
    private fun getImageUri(image: Bitmap): Uri {
        val file = File(requireActivity().cacheDir, "thumbnail")
        val outStream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.close()
        return Uri.fromFile(file)
    }

    /**
     * This method saves a reference of uploaded image in the database. The Firebase Storage does
     * NOT have a option to observe changes in the bucket an automatically updates the application.
     * We must use a database to have this feature in our application.
     *
     * @param url The public URL of uploaded image.
     * @param path The private URL of uploaded image on Firebase Storage.
     */
    private fun saveImageInDatabase(url: String, path: String) {
        val image = Image(url, path)

        // In the case of authenticated user, create a new unique key for the object in the
        // database.
        auth.currentUser?.let { user ->
            val uid = database.child("images")
                .child(user.uid)
                .push()
                .key

            // Insert the object in the database.
            uid?.let {
                database.child("images")
                    .child(user.uid)
                    .child(it)
                    .setValue(image)
            }
        }
    }

    override fun onItemClickListener(scooter: Scooter, position: Int) {
    }

}