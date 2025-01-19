package com.SKO.Taskmanager.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.activities.LoginActivity
import com.SKO.Taskmanager.classes.ApiClient
import com.SKO.Taskmanager.classes.DeletePhotoResponse
import com.SKO.Taskmanager.databinding.DialogDeleteBinding
import com.SKO.Taskmanager.databinding.DialogDeletePhotoBinding
import com.SKO.Taskmanager.databinding.DialogLogoutBinding
import com.SKO.Taskmanager.databinding.FragmentProfileBinding
import com.SKO.Taskmanager.interfaces.ApiInterface
import com.SKO.Taskmanager.responses.ProfilePhotoResponse
import com.SKO.Taskmanager.responses.UserDataClass
import com.SKO.Taskmanager.utils.SharedPreferencesUtil
import com.SKO.Taskmanager.utils.ToastUtil
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class ProfileFragment : Fragment() {

    private var apiInterface: ApiInterface? = null
    private var binding: FragmentProfileBinding? = null
    private val IMAGE_PICK_CODE = 1000 // Request code for image selection
    private val CAMERA_REQUEST_CODE = 1001 // Request code for camera capture
    private var capturedImageFile: File? = null
    private var progressDialog: ProgressDialog? = null
    private var profileImage:String?=null
    private val CAMERA_PERMISSION_REQUEST_CODE = 2000
    private val GALLERY_PERMISSION_REQUEST_CODE = 2001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        showProgressDialog()

        // Simulate a delay for fetching user data
        Handler(Looper.getMainLooper()).postDelayed({
            dismissProgressDialog()
            // Proceed with further logic, e.g., navigate to the next activity
        }, 1000)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiInterface = ApiClient.getMainClient(requireContext()).create(ApiInterface::class.java)

        getUserData()
        setupClickListeners()
    }

    private fun getUserData() {
        val call = apiInterface?.getUserData()
        call?.enqueue(object : Callback<UserDataClass> {
            override fun onResponse(call: Call<UserDataClass>, response: Response<UserDataClass>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    Log.d("User data:", user.toString())
                    profileImage=user?.image
                    // Set user details
                    binding!!.textViewProfilePageNameText.text =
                        "${user!!.firstName.replaceFirstChar { it.uppercase() }} ${user.lastName.replaceFirstChar { it.uppercase() }}"
                    binding!!.textViewProfilePageEmailText.text = user.email
                    setProfileImage()


                } else {
                    context?.let {
                        ToastUtil.showCustomToast(
                            it,
                            getString(R.string.error_fetching_user_data), false
                        )
                    }
                }
            }

            override fun onFailure(call: Call<UserDataClass>, t: Throwable) {
                context?.let {
                    ToastUtil.showCustomToast(
                        it,
                        getString(R.string.network_error_toast),
                        false
                    )
                }
                Log.e("ProfileFragment", "Error: ${t.message}", t)
            }
        })
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Failed to decode image: ${e.message}")
            null
        }
    }

    private fun setupClickListeners() {
        binding!!.buttonProfilePageEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding!!.buttonProfilePageChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileChangePasswordFragment)
        }

        binding!!.buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding!!.imageViewprofilePageimageEditIcon.setOnClickListener {
            showImageSourceDialog()
        }
    }


    private fun deleteProfilePhoto() {
        val call =
            apiInterface?.deleteProfilePhoto() // Assuming deleteProfilePhoto is a DELETE request in your API interface

        call?.enqueue(object : Callback<DeletePhotoResponse> {
            override fun onResponse(
                call: Call<DeletePhotoResponse>,
                response: Response<DeletePhotoResponse>
            ) {
                if (response.isSuccessful) {
                    // Clear the profile image on successful deletion
                    binding?.profilePageImageView?.setImageResource(R.drawable.profile_icon) // Use a default placeholder
                    profileImage=null
                    ToastUtil.showCustomToast(
                        requireContext(),
                        getString(R.string.profile_photo_deleted_successfully), true
                    )
                } else {
                    // Handle unsuccessful response
                    ToastUtil.showCustomToast(
                        requireContext(),
                        getString(R.string.failed_to_delete_profile_photo), false
                    )
                }
            }

            override fun onFailure(call: Call<DeletePhotoResponse>, t: Throwable) {
                // Handle network failure
                ToastUtil.showCustomToast(requireContext(), "Network error: ${t.message}", false)
            }
        })
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun captureImageWithCamera() {
        val outputDir = requireContext().cacheDir
        capturedImageFile = File.createTempFile("captured_", ".jpg", outputDir)

        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            capturedImageFile!!
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }

        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    val imageUri = data?.data
                    if (imageUri != null) {
                        val resizedFile = resizeImage(imageUri)
                        if (resizedFile != null) {
                            binding!!.profilePageImageView.setImageURI(imageUri)
                            uploadPhoto(resizedFile)
                        } else {
                            ToastUtil.showCustomToast(
                                requireContext(),
                                "Failed to process image",
                                false
                            )
                        }
                    } else {
                        ToastUtil.showCustomToast(requireContext(), "No image selected", false)
                    }
                }

                CAMERA_REQUEST_CODE -> {
                    capturedImageFile?.let { file ->
                        binding!!.profilePageImageView.setImageURI(Uri.fromFile(file))
                        uploadPhoto(file)
                    } ?: ToastUtil.showCustomToast(
                        requireContext(),
                        getString(R.string.failed_to_capture_image),
                        false
                    )
                }
            }
        }
    }

    private fun resizeImage(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val outputDir = requireContext().cacheDir
            val tempFile = File.createTempFile("resized_", ".jpg", outputDir)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.use {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                val byteArrayOutputStream = ByteArrayOutputStream()
                var quality = 90

                do {
                    byteArrayOutputStream.reset()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
                    quality -= 10
                } while (byteArrayOutputStream.size() > 60 * 1024 && quality > 10)

                outputStream.write(byteArrayOutputStream.toByteArray())
            }

            tempFile
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Resize error: ${e.message}")
            null
        }
    }

    private fun uploadPhoto(photoFile: File) {
        val mimeType = getString(R.string.image_jpeg)
        val fileBody = RequestBody.create(mimeType.toMediaTypeOrNull(), photoFile)
        val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, fileBody)

        val call = apiInterface?.uploadProfilePhoto(photoPart)
        call?.enqueue(object : Callback<ProfilePhotoResponse> {
            override fun onResponse(
                call: Call<ProfilePhotoResponse>,
                response: Response<ProfilePhotoResponse>
            ) {
                when {
                    response.isSuccessful && response.body()?.isSuccess == true -> {
                        // Success response (e.g., 200 OK)
                        ToastUtil.showCustomToast(
                            requireContext(),
                            getString(R.string.photo_updated_successfully),
                            true
                        )
                    }response.isSuccessful && response.body()?.isSuccess ==false -> {
                    // Success response (e.g., 200 OK)
                   setProfileImage()
                    ToastUtil.showCustomToast(
                        requireContext(),
                        getString(R.string.image_size_limit_is_60Kb),
                        false
                    )
                }

                    else -> {
                        // Handle other errors (e.g., 400, 404, etc.)
                        ToastUtil.showCustomToast(
                            requireContext(),
                            "Failed: ${response.errorBody()?.string()}",
                            false
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ProfilePhotoResponse>, t: Throwable) {
                ToastUtil.showCustomToast(requireContext(), "Error: ${t.message}", false)
            }
        })
    }


    private fun showLogoutConfirmationDialog() {
        val dialogBinding = DialogLogoutBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.buttonYes.setOnClickListener {
            performLogout()
            dialog.dismiss()
        }

        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showDeleteConfirmationDialog() {
        val dialogBinding = DialogDeletePhotoBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.buttonYes.setOnClickListener {
           deleteProfilePhoto()
            dialog.dismiss()
        }

        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun performLogout() {
        SharedPreferencesUtil.clearTokens(requireActivity())
        ToastUtil.showCustomToast(
            requireContext(),
            getString(R.string.logged_out_successfully), true
        )
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.fetching_user_details))
            setCancelable(false)
            show()
        }
    }

    private fun dismissProgressDialog() {
        progressDialog?.takeIf { it.isShowing }?.dismiss()
    }
    private fun setProfileImage(){
        if (!profileImage.isNullOrEmpty()) {
            val bitmap = profileImage?.let { decodeBase64ToBitmap(it) }
            if (bitmap != null) {
                Glide.with(requireContext())
                    .load(bitmap)
                    .placeholder(R.drawable.profile_icon)
                    .error(R.drawable.cancel)
                    .into(binding!!.profilePageImageView)
            }
        }else{
            binding?.profilePageImageView?.setImageResource(R.drawable.profile_icon)
        }
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_image_source))
            .setItems(arrayOf("Gallery", "Camera", "Delete Photo")) { _, which ->
                when (which) {
                    0 -> checkGalleryPermission()
                    1 -> checkCameraPermission()
                    2 -> showDeleteConfirmationDialog()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            // Explain why the permission is needed
            ToastUtil.showCustomToast(requireContext(), "Camera permission is required to take photos", false)
        }
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    private fun checkGalleryPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
            // Explain why the permission is needed
            ToastUtil.showCustomToast(requireContext(), "Gallery permission is required to pick images",false)
        }
        requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), GALLERY_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureImageWithCamera()
                } else {
                    ToastUtil.showCustomToast(requireContext(), "Camera permission denied",false)
                }
            }

            GALLERY_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    ToastUtil.showCustomToast(
                        requireContext(),
                        "Gallery permission denied",
                        false
                    )
                }
            }
        }
    }

}