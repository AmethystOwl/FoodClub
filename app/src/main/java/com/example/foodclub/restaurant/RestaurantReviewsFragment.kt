package com.example.foodclub.restaurant

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.foodclub.R
import com.example.foodclub.adapter.RestaurantReviewsAdapter
import com.example.foodclub.databinding.FragmentRestaurantReviewsBinding
import com.example.foodclub.databinding.LayoutBottomReviewBinding
import com.example.foodclub.model.Review
import com.example.foodclub.shared.UserViewModel
import com.example.foodclub.utils.DataState
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi


@AndroidEntryPoint
@OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class RestaurantReviewsFragment(private val restaurantId: String) : Fragment() {

    private val viewModel: RestaurantReviewsViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private var adapter: RestaurantReviewsAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRestaurantReviewsBinding.inflate(inflater, container, false)

        val query = viewModel.getReviewsQuery(restaurantId)
        val option = FirestoreRecyclerOptions.Builder<Review>().setQuery(query, Review::class.java).build()
        adapter = RestaurantReviewsAdapter(option)
        adapter?.startListening()
        binding.reviewsRecyclerView.adapter = adapter
        binding.writeReviewMaterialButton.setOnClickListener {
            val bsd = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
            val newReviewLayout = LayoutBottomReviewBinding.inflate(inflater, container, false)
            newReviewLayout.reviewTextInputEditText.imeOptions = EditorInfo.IME_ACTION_DONE
            newReviewLayout.reviewTextInputEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)
            newReviewLayout.reviewTextInputEditText.setOnEditorActionListener { textView, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val inputManager =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(
                        textView.windowToken,
                        HIDE_NOT_ALWAYS
                    )
                    textView.clearFocus()
                }
                true
            }
            bsd.setContentView(newReviewLayout.root)
            bsd.show()
            bsd.setOnShowListener {
                val dialog = it as BottomSheetDialog
                newReviewLayout.let { sheet ->
                    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    sheet.root.parent.parent.requestLayout()
                }
            }
            newReviewLayout.closeImageButton.setOnClickListener {
                bsd.dismiss()
            }
            newReviewLayout.postMaterialButton.setOnClickListener {
                userViewModel.currentUserProfile.value?.let { user ->
                    if (user.uId != null) {
                        val rating = newReviewLayout.materialRatingBar.rating
                        val reviewText = newReviewLayout.reviewTextInputEditText.text?.toString()
                        val review = Review(user.uId, user.name, user.profilePictureUrl, rating, reviewText)
                        viewModel.postReview(restaurantId, review)
                        bsd.dismiss()
                    }


                }
            }
            viewModel.reviewState.observe(viewLifecycleOwner) {
                when (it) {
                    is DataState.Loading -> {

                    }
                    is DataState.Success -> {
                        Toast.makeText(requireContext(), "Review posted", Toast.LENGTH_SHORT).show()
                    }
                    is DataState.Error -> {
                        Snackbar.make(requireView(), "${it.exception.message}", Snackbar.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter?.stopListening()
    }

}
