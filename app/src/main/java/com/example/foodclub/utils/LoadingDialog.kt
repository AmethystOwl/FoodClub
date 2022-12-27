package com.example.foodclub.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import com.example.foodclub.R

class LoadingDialog(private val activity: Activity) {
    private var dialog: AlertDialog? = null

    @SuppressLint("InflateParams")
    fun startDialog() {
        if(dialog != null && dialog?.isShowing!!) return
        val alertDialog = AlertDialog.Builder(activity)
            .setView(
                activity.layoutInflater
                    .inflate(
                        R.layout.dialog_layout,
                        null,
                        false
                    )
            ).setCancelable(false)
        dialog = alertDialog.create()
        dialog?.show()
    }

    fun dismissDialog() {
        dialog?.dismiss()
    }

}
