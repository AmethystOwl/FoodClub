package com.example.foodclub.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.databinding.ActivityMainBinding
import com.example.foodclub.utils.Constants
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navController = supportFragmentManager.findFragmentById(R.id.navHostFragmentContainer)
            ?.findNavController()
        binding.bottomNavView.setNavigationChangeListener { view, _ ->
            navController?.navigate(view.id)
        }
    }

    override fun onNavigateUp(): Boolean {
        return navController?.navigateUp()!! || super.onNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    fun showBottomNavigation() {
        binding.bottomNavView.visibility = View.VISIBLE
    }

    fun hideBottomNavigation() {
        binding.bottomNavView.visibility = View.GONE

    }

    fun setCurrentItemActive(position: Int) {
        binding.bottomNavView.setCurrentActiveItem(position)
    }


    fun changeLanguage(lang: String) {
        updateLocale(Locale(lang))
    }

    fun restartActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(R.anim.activity_alpha_in, R.anim.activity_alpha_out)
        finish()
    }


    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "Location Permission is required.",
            Constants.LOCATION_PERMISSION_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.d("mainActivity", "onPermissionsGranted: permission granted")
    }

}
