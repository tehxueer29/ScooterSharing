package dk.itu.moapd.scootersharing.xute.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.zxing.integration.android.IntentIntegrator
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.adapters.RealtimeAdapter
import dk.itu.moapd.scootersharing.xute.databinding.FragmentStartRideBinding
import dk.itu.moapd.scootersharing.xute.interfaces.ItemClickListener
import dk.itu.moapd.scootersharing.xute.models.Image
import dk.itu.moapd.scootersharing.xute.models.Scooter
import dk.itu.moapd.scootersharing.xute.utils.BUCKET_URL
import dk.itu.moapd.scootersharing.xute.utils.DATABASE_URL
import dk.itu.moapd.scootersharing.xute.utils.TAG
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
            adapter = RealtimeAdapter(requireContext(), this, "StartRideUI", options)

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

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // location settings are not turned on, show the user a dialog to turn it on
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        456
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // failed to show dialog
                }
            }
        }

        with(binding) {
//            Show all rides in maps listener
            viewMapsButton.setOnClickListener {
                findNavController().navigate(R.id.action_startRideFragment_to_mapsFragment)
            }
        }
    }

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
        // Upload the original image.
        image.putFile(uri).addOnSuccessListener { imageUrl ->

            // Upload the thumbnail image.
            thumbnail.putFile(createThumbnail(uri)).addOnSuccessListener {

                // Save the image reference in the database.
                imageUrl.metadata?.reference?.downloadUrl?.addOnSuccessListener { imageUri ->
                    saveImageInDatabase(imageUri.toString(), image.path)
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
//        Toast.makeText(requireContext(), "Button clicked for item at position $position", Toast.LENGTH_SHORT).show()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.start_ride))
            .setMessage(getString(R.string.alert_scanqr_text))
            .setNeutralButton(getString(R.string.cancel)) { _, _ ->
            }
            .setPositiveButton(getString(R.string.accept)) { _, _ ->
                val integrator = IntentIntegrator.forSupportFragment(this)
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                integrator.setPrompt("Scan a QR code")
                integrator.setCameraId(0) // Use the rear-facing camera
                integrator.setBeepEnabled(false)
                integrator.initiateScan()
            }
            .show()
    }

    private lateinit var scooter: Scooter
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            val scannedData = result.contents // The scanned QR code data
            // Do something with the scanned data
//            Log.i(TAG(), scannedData.toString())
            // Task completed successfully
            val scooterName: String? = scannedData
            val message = "You have started $scooterName!"
            Log.i(TAG(), message)

            auth.currentUser?.let { user ->
                val query =
                    database.child("scooter").orderByChild("name").equalTo(scooterName)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Do something here
                            Log.d(TAG(), dataSnapshot.value.toString())

                            if (dataSnapshot.value is ArrayList<*>) {
                                Log.d(TAG(), "trueE")
                                val dataSnapshot = dataSnapshot.value as ArrayList<*>
                                if (dataSnapshot[0] != null) {
                                    var result = dataSnapshot[0]
                                    result = result as Map<*, *>
                                    scooter = Scooter(
                                        result["name"] as String?,
                                        result["location"] as String?
                                    )
                                } else {
                                    var result = dataSnapshot[1]
                                    result = result as Map<*, *>
                                    scooter = Scooter(
                                        result["name"] as String?,
                                        result["location"] as String?
                                    )
                                }

                            } else {
                                val data = dataSnapshot.value as Map<*, *>
                                val result = data.values.first() as Map<*, *>
                                scooter = Scooter(
                                    result["name"] as String?,
                                    result["location"] as String?
                                )
                            }

//                                    Log.i(TAG(), scooter.toString())
                            // Handle retrieved data here
                            val uid =
                                database.child("rideHistory").child(user.uid).push().key

                            // Insert the object in the database.
                            uid?.let {
                                database.child("rideHistory").child(user.uid).child(it)
                                    .setValue(scooter)
                            }
                            findNavController().navigate(R.id.action_startRideFragment_to_rideHistoryFragment)

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
        }
    }

    override fun onLongItemClickListener(scooter: Scooter, position: Int) {
    }

}