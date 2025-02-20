package com.example.teams_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.example.teams_app.databinding.FragmentLocationTrackingBinding

class LocationTrackingFragment : Fragment() {
    private var _binding: FragmentLocationTrackingBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location granted
                startLocationUpdates()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Approximate location granted
                startLocationUpdates()
            }
            else -> {
                // No location access granted
                showLocationPermissionDeniedDialog()
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                updateLocationUI(location)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLocateMe.setOnClickListener {
            if (checkLocationPermissions()) {
                startLocationUpdates()
            } else {
                requestLocationPermissions()
            }
        }

        binding.btnOpenMaps.setOnClickListener {
            openInGoogleMaps()
        }

        binding.btnOpenStreetView.setOnClickListener {
            openInStreetView()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000
            ).build()

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                binding.tvLocationStatus.text = "Getting location..."
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error getting location: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateLocationUI(location: Location) {
        binding.tvLocationStatus.text = "Location found!"
        binding.tvLatitude.text = "Latitude: ${location.latitude}"
        binding.tvLongitude.text = "Longitude: ${location.longitude}"

        // Get address asynchronously
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        binding.tvAddress.text = buildString {
                            append("Address: ")
                            append(address.getAddressLine(0))
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvAddress.text = "Address: Unable to fetch address"
                }
            }
        }
    }

    private fun openInGoogleMaps() {
        try {
            val uri = Uri.parse("geo:$currentLatitude,$currentLongitude?q=$currentLatitude,$currentLongitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // If Google Maps app is not installed, open in browser
                val browserUri = Uri.parse(
                    "https://www.google.com/maps?q=$currentLatitude,$currentLongitude"
                )
                val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error opening maps: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openInStreetView() {
        try {
            val uri = Uri.parse(
                "google.streetview:cbll=$currentLatitude,$currentLongitude"
            )
            val streetViewIntent = Intent(Intent.ACTION_VIEW, uri)
            streetViewIntent.setPackage("com.google.android.apps.maps")

            if (streetViewIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(streetViewIntent)
            } else {
                // If Google Maps app is not installed, open in browser
                val browserUri = Uri.parse(
                    "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=$currentLatitude,$currentLongitude"
                )
                val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error opening Street View: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showLocationPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Location Permission Required")
            .setMessage("This feature requires location permission to work. Please grant location permission in settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}