package com.example.teams_app

import LocationData
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import java.util.Locale

class LocationTrackingFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var adapter: FriendLocationAdapter
    private val locationsList = mutableListOf<LocationData>()

    // View bindings
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvAddress: TextView
    private lateinit var fabShareLocation: FloatingActionButton
    private lateinit var rvFriendLocations: RecyclerView

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                updateLocationUI(location)
                updateLocationsList(location)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geocoder = Geocoder(requireContext(), Locale.getDefault())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_location_tracking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupRecyclerView()

        fabShareLocation.setOnClickListener {
            shareCurrentLocation()
        }

        if (checkLocationPermissions()) {
            startLocationUpdates()
        } else {
            requestLocationPermissions()
        }
    }

    private fun initializeViews(view: View) {
        tvLatitude = view.findViewById(R.id.tvLatitude)
        tvLongitude = view.findViewById(R.id.tvLongitude)
        tvAddress = view.findViewById(R.id.tvAddress)
        fabShareLocation = view.findViewById(R.id.fabShareLocation)
        rvFriendLocations = view.findViewById(R.id.rvFriendLocations)
    }

    private fun setupRecyclerView() {
        adapter = FriendLocationAdapter(locationsList) { location ->
            // Handle location click - e.g., show on map
            Toast.makeText(
                context,
                "Selected location: ${location.name} at ${location.address}",
                Toast.LENGTH_SHORT
            ).show()
        }
        rvFriendLocations.layoutManager = LinearLayoutManager(context)
        rvFriendLocations.adapter = adapter
    }

    private fun updateLocationsList(location: Location) {
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown address"

            val locationData = LocationData(
                id = System.currentTimeMillis().toString(),
                name = "Current Location",
                latitude = location.latitude,
                longitude = location.longitude,
                address = address,
                timestamp = System.currentTimeMillis()
            )

            locationsList.add(0, locationData) // Add to beginning of list
            if (locationsList.size > 10) { // Keep only last 10 locations
                locationsList.removeAt(locationsList.size - 1)
            }
            adapter.updateLocations(locationsList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startLocationUpdates() {
        if (checkLocationPermissions()) {
            try {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(5000)
                    .build()

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLocationUI(location: Location) {
        tvLatitude.text = "Latitude: ${location.latitude}"
        tvLongitude.text = "Longitude: ${location.longitude}"

        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.let { address ->
                val addressText = buildString {
                    append("Address: ")
                    append(address.getAddressLine(0))
                    address.locality?.let { append("\nCity: $it") }
                    address.adminArea?.let { append("\nState: $it") }
                    address.countryName?.let { append("\nCountry: $it") }
                }
                tvAddress.text = addressText
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tvAddress.text = "Address: Unable to fetch address"
        }
    }

    private fun shareCurrentLocation() {
        if (!checkLocationPermissions()) {
            requestLocationPermissions()
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location == null) {
                        Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown address"

                        val locationData = LocationData(
                            id = System.currentTimeMillis().toString(),
                            name = "Shared Location",
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = address,
                            timestamp = System.currentTimeMillis()
                        )

                        // Add to locations list
                        locationsList.add(0, locationData)
                        if (locationsList.size > 10) {
                            locationsList.removeAt(locationsList.size - 1)
                        }
                        adapter.updateLocations(locationsList)

                        val message = "Location shared successfully:\n" +
                                "Latitude: ${location.latitude}\n" +
                                "Longitude: ${location.longitude}\n" +
                                "Address: $address"

                        Toast.makeText(context, "Location shared!", Toast.LENGTH_SHORT).show()

                        // Here you can add your server upload logic
                        // uploadLocationToServer(locationData)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            context,
                            "Error processing location: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Error getting location: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (e: SecurityException) {
            Toast.makeText(
                context,
                "Location permission required",
                Toast.LENGTH_SHORT
            ).show()
            requestLocationPermissions()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                ) {
                    startLocationUpdates()
                } else {
                    Toast.makeText(
                        context,
                        "Location permissions are required for this feature",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (checkLocationPermissions()) {
            startLocationUpdates()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}