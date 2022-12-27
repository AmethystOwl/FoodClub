package com.example.foodclub.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mapbox.android.core.permissions.PermissionsListener

class LocationPermissionHelper(private var listener: PermissionsListener?) {

    private val LOG_TAG = "PermissionsManager"
    private val COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    private val FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    private val BACKGROUND_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION"

    private val REQUEST_PERMISSIONS_CODE = 0



    /**
     * Type of accuracy granted by a user to the app.
     */
    enum class AccuracyAuthorization {
        /** No location permission granted.  */
        NONE,

        /** Provides the location accuracy that the ACCESS_FINE_LOCATION permission provides.  */
        PRECISE,

        /** Provides the location accuracy that the ACCESS_COARSE_LOCATION permission provides.  */
        APPROXIMATE
    }


    fun getListener(): PermissionsListener? {
        return listener
    }

    fun setListener(listener: PermissionsListener?) {
        this.listener = listener
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun isCoarseLocationPermissionGranted(context: Context): Boolean {
        return isPermissionGranted(context, COARSE_LOCATION_PERMISSION)
    }

    private fun isFineLocationPermissionGranted(context: Context): Boolean {
        return isPermissionGranted(context, FINE_LOCATION_PERMISSION)
    }

    fun isBackgroundLocationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isPermissionGranted(context, BACKGROUND_LOCATION_PERMISSION)
        } else areLocationPermissionsGranted(context)
    }

    fun areLocationPermissionsGranted(context: Context): Boolean {
        return (isCoarseLocationPermissionGranted(context)
                || isFineLocationPermissionGranted(context))
    }

    fun areRuntimePermissionsRequired(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun requestLocationPermissions(fragment: Fragment) {
        try {
            val packageInfo = fragment.requireActivity().packageManager.getPackageInfo(
                fragment.requireActivity().packageName, PackageManager.GET_PERMISSIONS
            )
            val requestedPermissions = packageInfo.requestedPermissions
            if (requestedPermissions != null) {
                val permissionList = listOf(*requestedPermissions)
                val fineLocPermission = permissionList.contains(FINE_LOCATION_PERMISSION)
                val coarseLocPermission = permissionList.contains(COARSE_LOCATION_PERMISSION)
                val backgroundLocPermission =
                    permissionList.contains(BACKGROUND_LOCATION_PERMISSION)

                // Request location permissions
                if (fineLocPermission) {
                    requestLocationPermissions(fragment, true, backgroundLocPermission)
                } else if (coarseLocPermission) {
                    requestLocationPermissions(fragment, false, backgroundLocPermission)
                } else {
                    Log.w(LOG_TAG, "Location permissions are missing")
                }
            }
        } catch (exception: Exception) {
            Log.w(LOG_TAG, exception.message!!)
        }
    }

    private fun requestLocationPermissions(
        fragment: Fragment, requestFineLocation: Boolean,
        requestBackgroundLocation: Boolean
    ) {
        val permissions: MutableList<String> = ArrayList()
        if (requestFineLocation) {
            permissions.add(FINE_LOCATION_PERMISSION)
        } else {
            permissions.add(COARSE_LOCATION_PERMISSION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && requestBackgroundLocation) {
            permissions.add(BACKGROUND_LOCATION_PERMISSION)
        }
        requestPermissions(fragment, permissions.toTypedArray())
    }

    private fun requestPermissions(fragment: Fragment, permissions: Array<String>) {
        val permissionsToExplain = ArrayList<String>()
        for (permission in permissions) {
            if (fragment.shouldShowRequestPermissionRationale(permission)) {
                permissionsToExplain.add(permission)
            }
        }
        if (listener != null && permissionsToExplain.size > 0) {
            // The developer should show an explanation to the user asynchronously
            listener!!.onExplanationNeeded(permissionsToExplain)
        }
        fragment.requestPermissions(permissions, REQUEST_PERMISSIONS_CODE)
    }

    /**
     * You should call this method from your activity onRequestPermissionsResult.
     *
     * @param requestCode  The request code passed in requestPermissions(android.app.Activity, String[], int)
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either
     * PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>?,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSIONS_CODE -> if (listener != null) {
                val granted =
                    grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                listener!!.onPermissionResult(granted)
            }
            else -> {}
        }
    }

    /**
     * Returns the type of accuracy given by a user
     * to the application.
     *
     * @param context the application context
     * @return the type of accuracy authorization
     * @see AccuracyAuthorization
     */
    fun accuracyAuthorization(context: Context): AccuracyAuthorization {
        if (isFineLocationPermissionGranted(context)) {
            return AccuracyAuthorization.PRECISE
        }
        return if (isCoarseLocationPermissionGranted(context)) {
            AccuracyAuthorization.APPROXIMATE
        } else AccuracyAuthorization.NONE
    }
}

