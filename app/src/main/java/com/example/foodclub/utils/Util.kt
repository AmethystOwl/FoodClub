package com.example.foodclub.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.hideSoftKeyboard() {
    val inputManager =
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
        requireView().windowToken,
        InputMethodManager.HIDE_NOT_ALWAYS
    )
}

fun Fragment.showToast(text: String, isLong: Boolean) {
    Toast.makeText(requireContext(), text, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .show()
}

fun Fragment.showSnack(text: String, isLong: Boolean) {
    Snackbar.make(requireView(), text, if (isLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT)
        .show()
}