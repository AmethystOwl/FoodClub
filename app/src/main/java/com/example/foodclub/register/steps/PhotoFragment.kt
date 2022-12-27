package com.example.foodclub.register.steps

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.databinding.FragmentPhotoBinding
import com.example.foodclub.register.RegisterViewModel
import com.example.foodclub.utils.DataState
import com.example.foodclub.utils.LoadingDialog
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoFragment : Fragment() {
    private var imageUri: Uri? = null
    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        val resultCode = activityResult.resultCode
        val data = activityResult.data
        when (resultCode) {
            Activity.RESULT_OK -> {
                if (data != null && data.data != null) {
                    imageUri = data.data
                    binding.profilePictureImageView.setImageURI(data.data)
                    binding.buttonsGroup.visibility = View.GONE
                    binding.previewConstraintLayout.visibility = View.VISIBLE
                }

            }
            ImagePicker.RESULT_ERROR -> {
                imageUri = null
                binding.buttonsGroup.visibility = View.VISIBLE
                binding.previewConstraintLayout.visibility = View.GONE
            }
            else -> {
                imageUri = null
                binding.buttonsGroup.visibility = View.VISIBLE
                binding.previewConstraintLayout.visibility = View.GONE
            }
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).hideBottomNavigation()
        val loadingDialog = LoadingDialog(requireActivity())
        binding.cameraCardView.setOnClickListener {
            ImagePicker.with(this).cameraOnly().createIntent {
                getContent.launch(it)
            }
        }
        binding.galleryCardView.setOnClickListener {
            ImagePicker.with(this).galleryOnly().createIntent {
                getContent.launch(it)
            }
        }
        binding.clearButton.setOnClickListener {
            binding.profilePictureImageView.setImageDrawable(null)
            binding.buttonsGroup.visibility = View.VISIBLE
            binding.previewConstraintLayout.visibility = View.GONE

        }
        binding.nextButton.setOnClickListener {
            if (binding.profilePictureImageView.drawable != null && imageUri != null) {
                viewModel.uploadProfilePicture(imageUri!!)
            } else {
                findNavController().navigate(PhotoFragmentDirections.actionPhotoFragmentToLocationFragment())
            }
        }
        viewModel.uploadImageState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    loadingDialog.startDialog()
                }
                is DataState.Success -> {
                    loadingDialog.dismissDialog()
                    findNavController().navigate(PhotoFragmentDirections.actionPhotoFragmentToLocationFragment())
                }
                is DataState.Error -> {
                    loadingDialog.dismissDialog()
                    Snackbar.make(
                        binding.coordinator,
                        "${it.exception.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {
                    loadingDialog.dismissDialog()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
