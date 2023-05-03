package dk.itu.moapd.scootersharing.xute.fragments

import android.Manifest
import android.content.IntentSender
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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.xute.BuildConfig
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

    private lateinit var scooterLatLng: HashMap<String, LatLng>
    private lateinit var newCoord: LatLng
    private lateinit var mMap: GoogleMap

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
        mMap = googleMap

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

        // Check if the user allows the application to access the location-aware resources.
//        Log.i(TAG(), checkPermission().toString())
        if (checkPermission()) return@OnMapReadyCallback

        try {

            // Show the current device's location as a blue dot.
            googleMap.isMyLocationEnabled = true

            auth.currentUser?.let {

                // Create the search query.
                val scooterListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//    Log.i(TAG(), dataSnapshot.value.toString())
                        scooterLatLng = HashMap<String, LatLng>()

                        // Iterate through all the children in the data snapshot
                        for (scooterSnapshot in dataSnapshot.children) {
                            // Get the value of the scooter snapshot and update markers
                            val scooter = scooterSnapshot.getValue<Scooter>()
//                        Log.i(TAG(), scooter!!.location!!)
                            val locName = scooter?.location
                            val scooterName = scooter?.name.toString()

                            val geocoder = Geocoder(requireContext(), Locale.getDefault())
                            val results =
                                locName?.let { it1 -> geocoder.getFromLocationName(it1, 1) }
                            if (results != null && results.isNotEmpty()) {
                                val latitude = results[0].latitude
                                val longitude = results[0].longitude

//                            Log.i(TAG(), "Latitude: $latitude, Longitude: $longitude")
                                val locCoord = LatLng(latitude, longitude)
//                                    add scooter latitude and longitude into a hashmap
                                scooterLatLng[scooterName] = locCoord
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
                        newCoord = LatLng(
                            location.latitude, location.longitude
                        )

                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                newCoord, 14f
                            )
                        )

                    }
                }
            }


        } catch (e: SecurityException) {
            // Handle the exception appropriately
            Log.e(TAG(), e.toString())
        }

    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Start receiving location updates.
//        fusedLocationProviderClient =
//            LocationServices.getFusedLocationProviderClient(requireActivity())
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
                    newCoord = LatLng(
                        location.latitude, location.longitude
                    )

                }
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMapsBinding.inflate(
            layoutInflater, container, false
        )
        // Show a dialog to ask the user to allow the application to access the device's location.
        requestUserPermissions()

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
//        Initialize DB
        database = Firebase.database(DATABASE_URL).reference

        val locationRequestAccess = LocationRequest.create()
        locationRequestAccess.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequestAccess)
            .setAlwaysShow(true)
        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            // location settings are turned on

            // Start receiving location updates.
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())

            // Obtain the `SupportMapFragment` and get notified when the map is ready to be used.
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
        }
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nearestScooterButton.setOnClickListener {
//            Log.i(TAG(), scooterLatLng.toString())
            zoomNearestScooter()
//            Log.d(TAG(), "New coordinate x2: $newCoord")
//            Log.i(TAG(), newCoord.toString())

//            onclick, zoom into the scooter, show route there and make marker yellow(?)


        }
    }

    private fun zoomNearestScooter() {
        // Check if the user allows the application to access the location-aware resources.
        if (checkPermission())
            return
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            var userLatLng = LatLng(location.latitude, location.longitude)
            Log.d(TAG(), scooterLatLng.toString())
            Log.d(TAG(), "New coordinate: $userLatLng")

            var closestScooter = calculateDistanceToClosestMarker(userLatLng, scooterLatLng)
            var closestScooterName = closestScooter?.first
            var closestScooterDistance = closestScooter?.second
            var closestScooterLatLng = scooterLatLng[closestScooterName]

            //test cph 007 polyline
//            closestScooterName = "CPH007"
//            closestScooterLatLng = LatLng(55.665622299999995,12.600948899999999)
            Log.d(TAG(), "closest Scooter: $closestScooterName, $closestScooterLatLng")

// Create Directions API request URL
            val directionsApiUrl =
                "https://maps.googleapis.com/maps/api/directions/json?origin=${userLatLng.latitude},${userLatLng.longitude}&destination=${closestScooterLatLng?.latitude},${closestScooterLatLng?.longitude}&key=${BuildConfig.DIRECTIONS_API_KEY}"
//            Log.d(TAG(), directionsApiUrl)

// Send API request using Volley
            val requestQueue = Volley.newRequestQueue(requireContext())
            val directionsRequest = JsonObjectRequest(
                Request.Method.GET, directionsApiUrl, null,
                { response ->
                    // Parse response and get polyline coordinates
                    val points = response
                        .getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")
                    var decodedPoints = decodePolyline(points).toMutableList()
                    closestScooterLatLng?.let { decodedPoints.add(it) } // Adds to the end of the list
                    decodedPoints.add(0, userLatLng) // Adds to the front of the list

//                    Log.d(TAG(), points.toString())
                    Log.d(TAG(), decodedPoints.toString())

                    // Get the color from colors.xml
                    val polylineColor = ContextCompat.getColor(requireContext(), R.color.yellow_700)
                    val overlayColor = ContextCompat.getColor(requireContext(), R.color.yellow_1000)

                    val polylineOptions = PolylineOptions().apply {
                        addAll(decodedPoints)
                        width(15f)
                        color(polylineColor)
                        startCap(RoundCap())
                        endCap(RoundCap())
                        jointType(JointType.ROUND)
                    }
                    val polyline = mMap.addPolyline(polylineOptions)

                    // Define outline polyline options
                    val outlineOptions = PolylineOptions()
                        .addAll(decodedPoints)
                        .color(overlayColor)
                        .width(22f)
                        .startCap(RoundCap())
                        .endCap(RoundCap())
                        .jointType(JointType.ROUND)
                        .zIndex(polyline.zIndex - 1)

// Add outline polyline to the map
                    mMap.addPolyline(outlineOptions)
//
//                    val overlayOptions = PolylineOptions()
//                        .color(Color.WHITE) // set the color to white
//                        .width(15f) // set the width to 15 pixels
//                        .pattern(listOf(Dot(), Gap(20f))) // set the pattern to a dotted line
//                        .zIndex(polyline.zIndex + 1)
//
//                    mMap.addPolyline(overlayOptions)
                    if (closestScooterLatLng != null) {

                        val marker = mMap.addMarker(
                            MarkerOptions().position(closestScooterLatLng).title(closestScooterName)
                                .snippet("You are ${closestScooterDistance?.toInt()}m away.")
                        )
                        // Show info window for the marker
                        marker?.showInfoWindow()

                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    closestScooterLatLng.latitude, closestScooterLatLng.longitude
                                ), 17f
                            )
                        )
                    }
                },
                { error ->
                    // Handle error
                    Log.e(TAG(), "Directions API request failed: ${error.message}")
                })

            requestQueue.add(directionsRequest)
        }
    }

    fun decodePolyline(encodedPolyline: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encodedPolyline.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encodedPolyline[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encodedPolyline[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(latLng)
        }

        return poly
    }


    //    using haversine formula to calculate distance
    fun calculateDistanceToClosestMarker(
        currentLatLng: LatLng,
        scooterLocations: HashMap<String, LatLng>
    ): Pair<String, Double>? {
        var closestMarker: Pair<String, Double>? = null
        for ((scooterName, scooterLatLng) in scooterLocations) {
            val distance = calculateDistance(currentLatLng, scooterLatLng)
            if (closestMarker == null || distance < closestMarker.second) {
                closestMarker = Pair(scooterName, distance)
            }
        }
        return closestMarker
    }

    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val earthRadius = 6371000.0 // meters
        val latDistance = Math.toRadians(to.latitude - from.latitude)
        val lngDistance = Math.toRadians(to.longitude - from.longitude)
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + (Math.cos(Math.toRadians(from.latitude))
                * Math.cos(Math.toRadians(to.latitude))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2)))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c)
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

//
    /**
     * Subscribes this application to get the location changes via the `locationCallback()`.
     */
    private fun subscribeToLocationUpdates() {

        // Check if the user allows the application to access the location-aware resources.
        if (checkPermission())
            return

        // Sets the accuracy and desired interval for active location updates.
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
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