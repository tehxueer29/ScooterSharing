package dk.itu.moapd.scootersharing.xute.fragments

import android.Manifest
import dk.itu.moapd.scootersharing.xute.R
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
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
import dk.itu.moapd.scootersharing.xute.databinding.FragmentCameraBinding
import dk.itu.moapd.scootersharing.xute.models.Image
import dk.itu.moapd.scootersharing.xute.utils.BUCKET_URL
import dk.itu.moapd.scootersharing.xute.models.CameraFragmentVM
import dk.itu.moapd.scootersharing.xute.utils.DATABASE_URL
import dk.itu.moapd.scootersharing.xute.utils.TAG
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraFragment : Fragment() {
    /**
     * Using lazy initialization to create the view model instance when the user access the object
     * for the first time.
     */
    private val viewModel: CameraFragmentVM by lazy {
        ViewModelProvider(requireActivity())[CameraFragmentVM::class.java]
    }

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
     * The camera selector allows to select a camera or return a filtered set of cameras.
     */
    private lateinit var cameraSelector: CameraSelector

    private lateinit var scooterID: String

    /**
     * Defines a directory to save the captured images.
     */
    private lateinit var outputDirectory: File

    /**
     * An `Executor` that provides methods to manage termination and methods that can produce a
     * `Future` for tracking progress of one or more asynchronous tasks.
     */
    private lateinit var cameraExecutor: ExecutorService

    /**
     * This instance provides `takePicture()` functions to take a picture to memory or save to a
     * file, and provides image metadata.
     */
    private var imageCapture: ImageCapture? = null

    /**
     * The latest image taken by the video device stream.
     */
    private var imageUri: Uri? = null

    /**
     * A set of static attributes used in this activity class.
     */
    companion object {
        private val TAG = CameraFragment::class.qualifiedName
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    /**
     * View binding is a feature that allows you to more easily write code that interacts with
     * views. Once view binding is enabled in a module, it generates a binding class for each XML
     * layout file present in that module. An instance of a binding class contains direct references
     * to all views that have an ID in the corresponding layout.
     */
    private lateinit var binding: FragmentCameraBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(
            layoutInflater, container, false
        )


        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
        database = Firebase.database(DATABASE_URL).reference
        storage = Firebase.storage(BUCKET_URL)

        // Request camera permissions.
        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(
            requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )

        // Define the UI behavior.
        binding.contentCamera.apply {

            // Set up the listener for take photo button.
            cameraCaptureButton.setOnClickListener {
                binding.cameraInfo.visibility = View.GONE
                binding.loadingInfo.visibility = View.VISIBLE
                binding.contentCamera.cameraCaptureButton.isEnabled = false
                binding.contentCamera.cameraSwitchButton.isEnabled = false
                takePhoto()
            }

            // Set up the listener for the change camera button.
            cameraSwitchButton.let {

                // Disable the button until the camera is set up
                it.isEnabled = false

                // Listener for button used to switch cameras. Only called if the button is enabled
                it.setOnClickListener {
                    viewModel.onCameraSelectorChanged(
                        if (CameraSelector.DEFAULT_FRONT_CAMERA == cameraSelector) CameraSelector.DEFAULT_BACK_CAMERA
                        else CameraSelector.DEFAULT_FRONT_CAMERA
                    )

                    // Re-start use cases to update selected camera.
                    startCamera()
                }
            }
        }

        // The current selected camera.
        cameraSelector = viewModel.selector.value ?: CameraSelector.DEFAULT_BACK_CAMERA
        viewModel.selector.observe(requireActivity()) {
            cameraSelector = it
        }


        // Create and get the output directory.
        outputDirectory = getOutputDirectory()

        // Create an Executor that uses a single worker thread operating off an unbounded queue.
        cameraExecutor = Executors.newSingleThreadExecutor()

        return binding.root
    }

    /**
     * A method to show a dialog to the users as ask permission to access their Android mobile
     * device resources.
     *
     * @return `PackageManager#PERMISSION_GRANTED` if the given pid/uid is allowed that permission,
     *      or `PackageManager#PERMISSION_DENIED` if it is not.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * This method is used to start the video camera device stream.
     */
    private fun startCamera() {

        // Create an instance of the `ProcessCameraProvider` to bind the lifecycle of cameras to the
        // lifecycle owner.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        // Add a listener to the `cameraProviderFuture`.
        cameraProviderFuture.addListener({

            // Used to bind the lifecycle of cameras to the lifecycle owner.
            val cameraProvider = cameraProviderFuture.get()

            // Video camera streaming preview.
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.contentCamera.viewFinder.surfaceProvider)
            }

            // Set up the image capture by getting a reference to the `ImageCapture`.
            imageCapture = ImageCapture.Builder().build()
            imageCapture!!.targetRotation = binding.contentCamera.viewFinder.display.rotation

            try {
                // Unbind use cases before rebinding.
                cameraProvider.unbindAll()

                // Bind use cases to camera.
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // Update the camera switch button.
                updateCameraSwitchButton(cameraProvider)

            } catch (ex: Exception) {
                Log.e(TAG, "Use case binding failed", ex)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * This method is used to save a frame from the video camera device stream as a JPG photo.
     */
    private fun takePhoto() {

        // Get a stable reference of the modifiable image capture use case.
        val imageCapture: ImageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image.
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata.
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken.
        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {

                /**
                 * Called when an image has been successfully saved.
                 */
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    imageUri = Uri.fromFile(photoFile)
//                    push image to firebase storage DB
                    pushToStorage(imageUri!!)

                }

                /**
                 * Called when an error occurs while attempting to save an image.
                 *
                 * @param exception An `ImageCaptureException` that contains the type of error, the
                 *      error message and the throwable that caused it.
                 */
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            })
    }

    /**
     * Enabled or disabled a button to switch cameras depending on the available cameras.
     */
    private fun updateCameraSwitchButton(provider: ProcessCameraProvider) {
        try {
            binding.contentCamera.cameraSwitchButton.isEnabled =
                hasBackCamera(provider) && hasFrontCamera(provider)
        } catch (exception: CameraInfoUnavailableException) {
            binding.contentCamera.cameraSwitchButton.isEnabled = false
        }
    }

    /**
     * Returns true if the device has an available back camera. False otherwise.
     */
    private fun hasBackCamera(provider: ProcessCameraProvider) =
        provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

    /**
     * Returns true if the device has an available front camera. False otherwise.
     */
    private fun hasFrontCamera(provider: ProcessCameraProvider) =
        provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)

    /**
     * This method checks, creates and returns the output directory where the captured image will be
     * saved.
     *
     * @return A full path where the image will be saved.
     */
    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else requireContext().filesDir
    }

    /**
     * When the second activity finishes (i.e., the photo gallery intent), it returns a result to
     * this activity. If the user selects an image correctly, we can get a reference of the selected
     * image and send it to the Firebase Storage.
     *
     * @param result A container for an activity result as obtained form `onActivityResult()`.
     */
    private fun pushToStorage(uri: Uri) {
        // Create the folder structure save the selected image in the bucket.
        auth.currentUser?.let {
            val filename = UUID.randomUUID().toString()
            val image = storage.reference.child("images/$filename")
            val thumbnail = storage.reference.child("images/${filename}_thumbnail")
//            Log.d("ok123", thumbnail.toString())
            uploadImageToBucket(uri, image, thumbnail)

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
        uri: Uri, image: StorageReference, thumbnail: StorageReference
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
        val decode = BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(uri))
        val thumbnail = ThumbnailUtils.extractThumbnail(decode, size, size, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)

        // Rotate the thumbnail by 90 degrees clockwise
        val matrix = Matrix()
        matrix.postRotate(90f)
        val rotatedThumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.width, thumbnail.height, matrix, true)

        return getImageUri(rotatedThumbnail)
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
        val newPath = path.removePrefix("/images/")
        val image = Image(url, newPath)

        // In the case of authenticated user, create a new unique key for the object in the
        // database.
        auth.currentUser?.let { user ->

            setFragmentResultListener("requestKey") { key, bundle ->
                scooterID = bundle.getString("data").toString()
                Log.d(TAG, scooterID)

                val queryRideHistory =
                    database.child("rideHistory").child(user.uid).orderByChild("timestamp")
                        .equalTo(scooterID.toDouble())
                queryRideHistory.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Retrieve the key for the ride with the given timestamp
                            val rideKey = dataSnapshot.children.first().key

                            Log.d(TAG, dataSnapshot.value.toString())
                            Log.d(TAG, rideKey.toString())
//                            TODO add to scooter table
                            val data = dataSnapshot.value as Map<*, *>
                            val result = data.values.first() as Map<*, *>
//
                            val scooterName = result["name"].toString()
                            Log.d(TAG, scooterName.toString())

                            var index = scooterName.takeLast(3).toInt() - 1
                            Log.d(TAG, index.toString())

                            // Update the value
                            val updates = HashMap<String, Any>()
                            updates["image"] = image
                            updates["endTime"] = System.currentTimeMillis()

                            database.child("rideHistory").child(user.uid).child(rideKey!!)
                                .updateChildren(updates).addOnSuccessListener {
                                    // Successfully updated the value in the database
                                    database.child("scooter").child(index.toString()).child("image")
                                        .setValue(image)
                                        .addOnSuccessListener {
                                            // Successfully updated the value in the database
//                                            findNavController().navigate(R.id.action_cameraFragment_to_rideHistoryFragment)
                                            findNavController().popBackStack()
                                        }
                                }


                        } else {
                            // Child with name "cph00x" does not exist in the list of scooters
                            // Do something here
                            Log.w(
                                TAG(), "does not exist."
                            )
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

    /**
     * Make a standard toast that just contains text.
     *
     * @param text The text to show. Can be formatted text.
     * @param duration How long to display the message. Either `Toast.LENGTH_SHORT` or
     *      `Toast.LENGTH_LONG`.
     */
    private fun snackBar(
        text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT
    ) {
        Snackbar.make(binding.root, text, duration).show()
    }

}