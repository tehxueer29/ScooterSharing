package dk.itu.moapd.scootersharing.xute.fragments

import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.databinding.FragmentMapsBinding

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

    //
    private val callback = OnMapReadyCallback { googleMap ->
//        /**
//         * Manipulates the map once available.
//         * This callback is triggered when the map is ready to be used.
//         * This is where we can add markers or lines, add listeners or move the camera.
//         * In this case, we just add a marker near Sydney, Australia.
//         * If Google Play services is not installed on the device, the user will be prompted to
//         * install it inside the SupportMapFragment. This method will only be triggered once the
//         * user has installed Google Play services and returned to the app.
//         */
//        val sydney = LatLng(-34.0, 151.0)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        // Check if the user allows the application to access the location-aware resources.
        if (checkPermission())
            return@OnMapReadyCallback

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

        } catch (e: SecurityException) {
            // Handle the exception appropriately
        }

        // Move the Google Maps UI buttons under the OS top bar.
        googleMap.setPadding(0, 100, 0, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMapsBinding.inflate(
            layoutInflater, container, false
        )

        // Obtain the `SupportMapFragment` and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        // Show a dialog to ask the user to allow the application to access the device's location.
        requestUserPermissions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
//        mapFragment?.getMapAsync(callback)
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
        if (permissionsToRequest.size > 0)
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                ALL_PERMISSIONS_RESULT
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
        for (permission in permissions)
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            )
                result.add(permission)
        return result
    }

    /**
     * This method checks if the user allows the application uses all location-aware resources to
     * monitor the user's location.
     *
     * @return A boolean value with the user permission agreement.
     */
    private fun checkPermission() =
        ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED

}