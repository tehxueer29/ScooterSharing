package dk.itu.moapd.scootersharing.xute.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.databinding.FragmentMapsBinding
import dk.itu.moapd.scootersharing.xute.models.Scooter
import dk.itu.moapd.scootersharing.xute.utils.DATABASE_URL
import dk.itu.moapd.scootersharing.xute.utils.TAG
import java.util.*
import kotlin.collections.ArrayList

class MapsFragment : Fragment() {

    /**
     * A set of static attributes used in this activity class.
     */
    companion object {
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

    /**
     * View binding is a feature that allows you to more easily write code that interacts with
     * views. Once view binding is enabled in a module, it generates a binding class for each XML
     * layout file present in that module. An instance of a binding class contains direct references
     * to all views that have an ID in the corresponding layout.
     */
    private lateinit var binding: FragmentMapsBinding

    /**
     * The primary instance for receiving location updates.
     */
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    /**
     * This callback is called when `FusedLocationProviderClient` has a new `Location`.
     */
    private lateinit var locationCallback: LocationCallback

    /**
     * The entry point of the Firebase Authentication SDK.
     */
    private lateinit var auth: FirebaseAuth

    /**
     * A Firebase reference represents a particular location in your Database and can be used for
     * reading or writing data to that Database location.
     */
    private lateinit var database: DatabaseReference

    //
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
//        val sydney = LatLng(-34.0, 151.0)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        // Check if the user allows the application to access the location-aware resources.
        if (checkPermission()) return@OnMapReadyCallback


        try {
            // Call the method or operation that requires the permission
            // Show the current device's location as a blue dot.
            googleMap.isMyLocationEnabled = true

            // Set the default map type.
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

            // Setup the UI settings state.
            googleMap.uiSettings.apply {
                isCompassEnabled = true
                isIndoorLevelPickerEnabled = true
                isMyLocationButtonEnabled = true
                isRotateGesturesEnabled = true
                isScrollGesturesEnabled = true
                isTiltGesturesEnabled = true
                isZoomControlsEnabled = true
                isZoomGesturesEnabled = true
            }

            // Move the Google Maps UI buttons under the OS top bar.
            googleMap.setPadding(0, 100, 0, 0)

            auth.currentUser?.let {

                // Create the search query.
                val scooterListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//    Log.i(TAG(), dataSnapshot.value.toString())
                        // Iterate through all the children in the data snapshot
                        for (scooterSnapshot in dataSnapshot.children) {
                            // Get the value of the scooter snapshot and update markers
                            val scooter = scooterSnapshot.getValue<Scooter>()
//                        Log.i(TAG(), scooter!!.location!!)
                            val locName = scooter?.location
                            val scooterName = scooter?.name

                            val geocoder = Geocoder(requireContext(), Locale.getDefault())
                            val results =
                                locName?.let { it1 -> geocoder.getFromLocationName(it1, 1) }
                            if (results != null && results.isNotEmpty()) {
                                val latitude = results[0].latitude
                                val longitude = results[0].longitude
//                            Log.i(TAG(), "Latitude: $latitude, Longitude: $longitude")
                                val locCoord = LatLng(latitude, longitude)
                                googleMap.addMarker(
                                    MarkerOptions().position(locCoord).title(scooterName)
                                        .snippet(locName)
                                )
                            } else {
                                Log.d(TAG(), "No location found")
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Getting Post failed, log a message
                        Log.w(TAG(), "loadPost:onCancelled", error.toException())

                    }
                }
                database.child("scooter").addValueEventListener(scooterListener)
            }

            // Start receiving location updates.
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())

            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude, location.longitude
                            ), 14f
                        )
                    )
                }
            }


//             Initialize the `LocationCallback`.
            locationCallback = object : LocationCallback() {

                /**
                 * This method will be executed when `FusedLocationProviderClient` has a new location.
                 *
                 * @param locationResult The last known location.
                 */
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)


                    // Updates the user interface components with GPS data location.
                    locationResult.lastLocation?.let { location ->
//                        Log.d(TAG(), location.latitude.toString())
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location.latitude, location.longitude
                                ), 14f
                            )
                        )

                    }
                }
            }

        } catch (e: SecurityException) {
            // Handle the exception appropriately
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMapsBinding.inflate(
            layoutInflater, container, false
        )
        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
//        Initialize DB
        database = Firebase.database(DATABASE_URL).reference

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtain the `SupportMapFragment` and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        // Show a dialog to ask the user to allow the application to access the device's location.
        requestUserPermissions()
    }

    /**
     * Called after `onStart()`, `onRestart()`, or `onPause()`, for your activity to start
     * interacting with the user. This is an indicator that the activity became active and ready to
     * receive input. It is on top of an activity stack and visible to user.
     *
     * On platform versions prior to `android.os.Build.VERSION_CODES#Q` this is also a good place to
     * try to open exclusive-access devices or to get access to singleton resources. Starting  with
     * `android.os.Build.VERSION_CODES#Q` there can be multiple resumed activities in the system
     * simultaneously, so `onTopResumedActivityChanged(boolean)` should be used for that purpose
     * instead.
     *
     * <Derived classes must call through to the super class's implementation of this method. If
     * they do not, an exception will be thrown.
     */
    override fun onResume() {
        super.onResume()
        subscribeToLocationUpdates()
    }

    /**
     * Called as part of the activity lifecycle when the user no longer actively interacts with the
     * activity, but it is still visible on screen. The counterpart to `onResume()`.
     *
     * When activity `B` is launched in front of activity `A`, this callback will be invoked on `A`.
     * `B` will not be created until `A`'s onPause() returns, so be sure to not do anything lengthy
     * here.
     *
     * This callback is mostly used for saving any persistent state the activity is editing, to
     * present a "edit in place" model to the user and making sure nothing is lost if there are not
     * enough resources to start the new activity without first killing this one. This is also a
     * good place to stop things that consume a noticeable amount of CPU in order to make the switch
     * to the next activity as fast as possible.
     *
     * On platform versions prior to `android.os.Build.VERSION_CODES#Q` this is also a good place to
     * try to close exclusive-access devices or to release access to singleton resources. Starting
     * with `android.os.Build.VERSION_CODES#Q` there can be multiple resumed activities in the
     * system at the same time, so `onTopResumedActivityChanged(boolean)` should be used for that
     * purpose instead.
     *
     * If an activity is launched on top, after receiving this call you will usually receive a
     * following call to `onStop()` (after the next activity has been resumed and displayed above).
     * However in some cases there will be a direct call back to `onResume()` without going through
     * the stopped state. An activity can also rest in paused state in some cases when in
     * multi-window mode, still visible to user.
     *
     * Derived classes must call through to the super class's implementation of this method. If they
     * do not, an exception will be thrown.
     */
    override fun onPause() {
        super.onPause()
        unsubscribeToLocationUpdates()
    }
    /**
     * Create a set of dialogs to show to the users and ask them for permissions to get the device's
     * resources.
     */
    private fun requestUserPermissions() {

        // An array with location-aware permissions.
        val permissions: ArrayList<String> = ArrayList()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Check which permissions is needed to ask to the user.
        val permissionsToRequest = permissionsToRequest(permissions)

        // Show the permissions dialogs to the user.
        if (permissionsToRequest.size > 0) requestPermissions(
            permissionsToRequest.toTypedArray(), ALL_PERMISSIONS_RESULT
        )
    }

    /**
     * Create an array with the permissions to show to the user.
     *
     * @param permissions An array with the permissions needed by this applications.
     *
     * @return An array with the permissions needed to ask to the user.
     */
    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions) if (ContextCompat.checkSelfPermission(
                requireContext(), permission
            ) != PackageManager.PERMISSION_GRANTED
        ) result.add(permission)
        return result
    }

    /**
     * This method checks if the user allows the application uses all location-aware resources to
     * monitor the user's location.
     *
     * @return A boolean value with the user permission agreement.
     */
    private fun checkPermission() = ActivityCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED


    /**
     * Subscribes this application to get the location changes via the `locationCallback()`.
     */
    private fun subscribeToLocationUpdates() {

        // Check if the user allows the application to access the location-aware resources.
        if (checkPermission())
            return

        // Sets the accuracy and desired interval for active location updates.
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 5)
            .build()

        // Subscribe to location changes.
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    /**
     * Unsubscribes this application of getting the location changes from  the `locationCallback()`.
     */
    private fun unsubscribeToLocationUpdates() {
        // Unsubscribe to location changes.
        fusedLocationProviderClient
            .removeLocationUpdates(locationCallback)
    }

}