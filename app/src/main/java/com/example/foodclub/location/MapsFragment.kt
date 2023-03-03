package com.example.foodclub.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.databinding.FragmentMapsBinding
import com.example.foodclub.databinding.LayoutBottomLocationBinding
import com.example.foodclub.model.UserLocation
import com.example.foodclub.shared.UserViewModel
import com.example.foodclub.utils.DataState
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.core.location.*
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference


@AndroidEntryPoint
class MapsFragment : Fragment() {
    private val TAG = "MapsFragment"

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                onMapReady()
            }
        }
    private val permissionsActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (!perms.containsValue(true)) {
                Toast.makeText(
                    requireContext(),
                    "Location permission is required.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                checkPermissionSettings()
            }
        }

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var mapView: MapView
    private var locationEngine: LocationEngine? = null
    private var location: Location? = null
    private lateinit var callback: LocationListeningCallback
    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
        // latestPoint = it

    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).hideBottomNavigation()
        callback = object : LocationListeningCallback(this) {
            override fun onSuccess(result: LocationEngineResult) {
                super.onSuccess(result)
                Log.d(TAG, "onSuccess: ${result.lastLocation}")
                location = result.lastLocation

            }

            override fun onFailure(exception: Exception) {
                super.onFailure(exception)
                Log.d(TAG, "onFailure: ${exception.localizedMessage}")

            }

        }
        if (checkPermissions()) {
            checkPermissionSettings()
        } else {
            permissionsActivityResult.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        }

        binding.locateMeImageButton.setOnClickListener {
            if (checkPermissions()) {
                onMapReady()
            }
        }

        binding.setLocationMaterialButton.setOnClickListener {


            val bottomLocationBinding = LayoutBottomLocationBinding.inflate(layoutInflater)
            val alertDialog =
                BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
            alertDialog.setContentView(bottomLocationBinding.root)
            alertDialog.show()
            bottomLocationBinding.saveButton.setOnClickListener {
                val street = bottomLocationBinding.streetNameTextInputEditText.text?.toString()
                val building = bottomLocationBinding.buildingNoTextInputEditText.text?.toString()
                val floor = bottomLocationBinding.floorTextInputEditText.text?.toString()
                val apartment = bottomLocationBinding.apartmentInputEditText.text?.toString()
                if (street.isNullOrBlank() || street.isEmpty()) {
                    bottomLocationBinding.streetNameTextInputLayout.error = "Required"
                    bottomLocationBinding.streetNameTextInputLayout.requestFocus()
                } else if (building.isNullOrBlank() || building.isEmpty()) {
                    bottomLocationBinding.buildingNoInputLayout.error = "Required"
                    bottomLocationBinding.buildingNoInputLayout.requestFocus()
                } else if (floor.isNullOrBlank() || floor.isEmpty()) {
                    bottomLocationBinding.floorInputLayout.error = "Required"
                    bottomLocationBinding.floorInputLayout.requestFocus()
                } else if (apartment.isNullOrBlank() || apartment.isEmpty()) {
                    bottomLocationBinding.apartmentInputLayout.error = "Required"
                    bottomLocationBinding.apartmentInputLayout.requestFocus()
                } else {
                    val userLocation = UserLocation(
                        location?.latitude,
                        location?.longitude,
                        street,
                        building.toInt(),
                        floor.toInt(),
                        apartment.toInt()
                    )
                    userViewModel.updateLocation(userLocation)
                    alertDialog.dismiss()

                }
            }
        }
        userViewModel.locationState.observe(viewLifecycleOwner) { locationState ->
            when (locationState) {
                is DataState.Loading -> {
                }
                is DataState.Success -> {
                    userViewModel.onDoneObservingLocation()
                    userViewModel.setUserLocation(locationState.data)
                    Toast.makeText(requireContext(), "Location Updated", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigateUp()
                }
                is DataState.Error -> {
                    Snackbar.make(
                        binding.root,
                        "An error has occurred : ${locationState.exception.localizedMessage}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {
                }
            }
        }
    }

    private fun checkPermissionSettings() {
        val locationRequest = LocationRequest.Builder(5000L).build()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val result = LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())
        result.addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    onMapReady()
                }
                it.exception != null -> {
                    val exception = it.exception as ApiException
                    when (exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            try {
                                val resolvableApiException =
                                    exception as ResolvableApiException
                                val intentSenderRequest =
                                    IntentSenderRequest.Builder(resolvableApiException.resolution)
                                        .build()
                                resolutionForResult.launch(intentSenderRequest)

                            } catch (e: Exception) {
                                Log.d(
                                    TAG,
                                    "onViewCreated: There was no Activity found to run the given Intent."
                                )
                            }
                        }
                        else -> {
                            Toast.makeText(
                                requireContext(),
                                it.exception?.localizedMessage!!,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

        }
    }

    private fun checkPermissions(): Boolean {
        return checkFineLocation() && checkCoarseLocation()
    }

    private fun checkFineLocation(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    }

    private fun checkCoarseLocation(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun onMapReady() {
        mapView = binding.mapView
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
            binding.setLocationMaterialButton.isEnabled = true
        }
        locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
        if (checkPermissions()) {
            locationEngine?.getLastLocation(callback)
        }
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.pulsingEnabled = true
            this.pulsingColor = requireContext().getColor(R.color.primary)
            this.locationPuck = LocationPuck2D(
                topImage = AppCompatResources.getDrawable(
                    requireContext(),
                    com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_icon
                ),
                shadowImage = AppCompatResources.getDrawable(
                    requireContext(),
                    com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_stroke_icon
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()

            )

        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )

    }

    private fun onCameraTrackingDismissed() {
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        locationEngine?.removeLocationUpdates(callback)
    }

    private open class LocationListeningCallback(fragment: Fragment) :
        LocationEngineCallback<LocationEngineResult> {

        private val activityWeakReference: WeakReference<Fragment>

        init {
            this.activityWeakReference = WeakReference(fragment)
        }

        override fun onSuccess(result: LocationEngineResult) {
            // The LocationEngineCallback interface's method which fires when the device's location has changed.
            result.lastLocation

        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        override fun onFailure(exception: Exception) {
            // The LocationEngineCallback interface's method which fires when the device's location can not be captured
        }

    }
}

