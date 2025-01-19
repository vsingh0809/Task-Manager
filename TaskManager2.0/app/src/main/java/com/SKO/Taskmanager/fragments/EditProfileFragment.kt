package com.SKO.Taskmanager.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.activities.RegisterActivity
import com.SKO.Taskmanager.classes.ApiClient
import com.SKO.Taskmanager.classes.EditProfileRequest
import com.SKO.Taskmanager.databinding.DialogEditProfileBinding
import com.SKO.Taskmanager.databinding.DialogLogoutBinding
import com.SKO.Taskmanager.databinding.DialogVerifyOtpBinding
import com.SKO.Taskmanager.databinding.FragmentEditProfileBinding
import com.SKO.Taskmanager.interfaces.ApiInterface
import com.SKO.Taskmanager.responses.EditProfileResponse
import com.SKO.Taskmanager.responses.SendOtpRequest
import com.SKO.Taskmanager.responses.StatusResponse
import com.SKO.Taskmanager.responses.UserDataClass
import com.SKO.Taskmanager.responses.VerifyEmailResponse
import com.SKO.Taskmanager.responses.VerifyOtpRequest
import com.SKO.Taskmanager.utils.ToastUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileFragment : Fragment() {

    private var isValid: Boolean = true
    private var apiInterface: ApiInterface? = null
    private var binding: FragmentEditProfileBinding? = null
    private var userName: String? = null
    private var initialEmail:String?=null
    private var flag:Boolean=false
    private var toolbar : androidx.appcompat.widget.Toolbar ?= null
    private var progressDialog: ProgressDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        binding?.textViewProfileVerifyEmail?.visibility = View.GONE
        binding?.progressBarEditProfile?.visibility = View.GONE

        toolbar = binding?.toolbarInclude?.toolbar
        showProgressDialog()

        // Simulate a delay for fetching user data
        Handler(Looper.getMainLooper()).postDelayed({
            dismissProgressDialog()
        }, 1000)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        apiInterface = ApiClient.getMainClient(requireContext()).create(ApiInterface::class.java)
        getUserData()
        addTextWatchers()
        binding?.resetProfileButton?.setOnClickListener {
            binding?.editTextFirstName?.text?.clear()
            binding?.editTextLastName?.text?.clear()
            binding?.editTextUserEmail?.text?.clear()
        }

        binding?.editTextUserEmail?.doAfterTextChanged { email ->
            handleEmailChange(email.toString())
        }

        binding?.textViewProfileVerifyEmail?.setOnClickListener {
            verifyEmail()
        }



        binding?.saveProfileButton?.setOnClickListener {
            showSaveProfileConfirmationDialog()
        }
    }

    private fun handleEmailChange(email: String) {
        if(flag==false){
            flag=true// Assuming the hint holds the initial email
        }
        if (email.isNotEmpty() && email != initialEmail && Patterns.EMAIL_ADDRESS.matcher(email)
                .matches()
        ) {
            binding?.textViewProfileVerifyEmail?.visibility = View.VISIBLE
            binding?.textViewProfileVerifyEmail?.isEnabled = true
            binding?.saveProfileButton?.isEnabled=false
        } else if (email == initialEmail) {
            binding?.textViewProfileVerifyEmail?.visibility = View.GONE
            binding?.textViewProfileVerifyEmail?.isEnabled = false
        } else {
            binding?.textViewProfileVerifyEmail?.visibility = View.GONE
        }
    }

//    private fun getUserData() {
//        val call = apiInterface?.getUserData()
//        call?.enqueue(object : Callback<UserDataClass> {
//            override fun onResponse(call: Call<UserDataClass>, response: Response<UserDataClass>) {
//                if (response.isSuccessful) {
//                    val user = response.body()
//                    Log.d("User data:", user.toString())
//
//                    // Set user details
//                    binding?.editTextFirstName?.setText(user?.firstName?.replaceFirstChar { it.uppercase() })
//                    binding?.editTextLastName?.setText(user?.lastName?.replaceFirstChar { it.uppercase() })
//                    binding?.editTextUserEmail?.setText(user?.email)
//                    initialEmail=user?.email
//                    userName = user?.username
//                } else {
//                    context?.let {
//                        ToastUtil.showCustomToast(
//                            it,  getString(R.string.network_request_failed_toast),
//                            false
//                        )
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<UserDataClass>, t: Throwable) {
//                context?.let { ToastUtil.showCustomToast(it, getString(R.string.network_error_toast), false) }
//                Log.e("ProfileFragment", "Error: ${t.message}", t)
//            }
//        })
//    }
    private var initialFirstName: String? = null
    private var initialLastName: String? = null


    private fun getUserData() {
        val call = apiInterface?.getUserData()
        call?.enqueue(object : Callback<UserDataClass> {
            override fun onResponse(call: Call<UserDataClass>, response: Response<UserDataClass>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    Log.d("User data:", user.toString())

                    // Set user details
                    binding?.editTextFirstName?.setText(user?.firstName?.replaceFirstChar { it.uppercase() })
                    binding?.editTextLastName?.setText(user?.lastName?.replaceFirstChar { it.uppercase() })
                    binding?.editTextUserEmail?.setText(user?.email)
                    initialFirstName = user?.firstName
                    initialLastName = user?.lastName
                    initialEmail = user?.email
                    userName = user?.username
                } else {
                    context?.let {
                        ToastUtil.showCustomToast(it, getString(R.string.network_request_failed_toast), false)
                    }
                }
            }

            override fun onFailure(call: Call<UserDataClass>, t: Throwable) {
                context?.let { ToastUtil.showCustomToast(it, getString(R.string.network_error_toast), false) }
                Log.e("ProfileFragment", "Error: ${t.message}", t)
            }
        })
    }



    private fun verifyEmail() {
        val email = binding?.editTextUserEmail?.text.toString().trim()
        val name = binding?.editTextFirstName?.text.toString().trim()

        if (RegisterActivity.ValidationUtils.isEmpty(name)) {
            binding?.textInputLayoutUserEmail?.error = getString(R.string.first_name_is_required)
            binding?.editTextFirstName?.requestFocus()
            isValid = false
        } else {
            binding?.textInputLayoutFirstName?.error = null
        }



        if (RegisterActivity.ValidationUtils.isEmpty(email)) {
            binding?.textInputLayoutUserEmail?.error = getString(R.string.email_is_required)
            binding?.editTextUserEmail?.requestFocus()
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding?.textInputLayoutUserEmail?.error = getString(R.string.invalid_email_format)
            binding?.editTextUserEmail?.requestFocus()
            isValid = false
        } else {
            binding?.textInputLayoutUserEmail?.error = null
        }

        if (isValid) {

            val call: Call<VerifyEmailResponse>? = apiInterface?.verifyEmail(email)
            call?.enqueue(object : Callback<VerifyEmailResponse> {
                override fun onResponse(
                    call: Call<VerifyEmailResponse>,
                    response: Response<VerifyEmailResponse>
                ) {
                    when {
                        response.isSuccessful -> {
                            if (response.body()?.emailExists == true) {
                                // User already exists, show a message and redirect to login
                                ToastUtil.showCustomToast(
                                    requireContext(),
                                    getString(R.string.user_already_exists_give_a_different_email),
                                    false
                                )
                            } else {
                                val call: Call<StatusResponse> =
                                    apiInterface!!.sendOtp(SendOtpRequest(email, name))
                                call.enqueue(object : Callback<StatusResponse> {
                                    override fun onResponse(
                                        call: Call<StatusResponse>,
                                        response: Response<StatusResponse>
                                    ) {
                                        if (response.isSuccessful) {
                                            ToastUtil.showCustomToast(
                                                requireContext(),
                                                "${response.body()?.message.toString()}",
                                                true
                                            )
                                            showOtpDialog(email, name)
                                            isValid = true
                                        } else if (response.code() == 500) {
                                            ToastUtil.showCustomToast(
                                                requireContext(),
                                                getString(R.string.invalid_email_formate),
                                                false
                                            )
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<StatusResponse>,
                                        response: Throwable
                                    ) {
                                        ToastUtil.showCustomToast(
                                            requireContext(),
                                            getString(R.string.network_request_failed),
                                            false
                                        )
                                        isValid = false
                                    }
                                })
                            }
                        }
                        response.code() == 500 -> {
                            ToastUtil.showCustomToast(
                                requireContext(),
                                getString(R.string.invalid_email_format),
                                false
                            )
                        }
                        else -> {
                            ToastUtil.showCustomToast(
                                requireContext(),
                                getString(R.string.network_request_failed_toast),
                                false
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<VerifyEmailResponse>, t: Throwable) {
                    ToastUtil.showCustomToast(
                        requireContext(),
                        getString(R.string.network_request_failed_toast),
                        false
                    )
                }
            })

        }
    }


    private fun showOtpDialog(email: String, name: String) {
        val dialogBinding = DialogVerifyOtpBinding.inflate(LayoutInflater.from(requireContext()))

        val otpDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)  // Use the root view of the ViewBinding
            .setPositiveButton(getString(R.string.resend_otp)) { dialog, _ ->
                resendOtp(email, name)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()


        dialogBinding.buttonVerifyOtp.setOnClickListener {
            val otp = dialogBinding.otpEditText.text.toString().trim()

            if (otp.isEmpty()) {
//                Toast.makeText(this@RegisterActivity, "OTP is required", Toast.LENGTH_SHORT).show()
                ToastUtil.showCustomToast(requireContext(),
                    getString(R.string.otp_is_required), false)


            } else {
                verifyOtp(otp, email)
                otpDialog.dismiss()
            }

        }

        // Show the dialog
        otpDialog.show()
    }


    private fun verifyOtp(otp: String, email: String) {

        val verifyOtpRequest = VerifyOtpRequest(email, otp)

        val call: Call<String>? = apiInterface?.verifyOtp(verifyOtpRequest)
        call?.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.isSuccessful && response.body() != null) {
                    // Success case
                    if (response.body().equals(getString(R.string.otp_is_valid))) {
                        ToastUtil.showCustomToast(
                            requireContext(),
                            getString(R.string.email_verified_successfully),
                            true
                        )
                        binding?.saveProfileButton?.isEnabled=true
                    } else {
                        ToastUtil.showCustomToast(requireContext(),
                            getString(R.string.invalid_otp), false)
                    }
                } else {
                    // Handle error case (plain string response)
                    val errorMessage = response.errorBody()?.string()
                        ?: getString(R.string.invalid_or_expired_otp) // Default message
                    ToastUtil.showCustomToast(requireContext(), errorMessage, false)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                //Toast.makeText(this@RegisterActivity, "Network request failed", Toast.LENGTH_SHORT).show()
                ToastUtil.showCustomToast(requireContext(), getString(R.string.network_request_failed_toast), false)
            }
        })
    }

    private fun resendOtp(email: String, name: String) {

        val call: Call<StatusResponse> = apiInterface!!.sendOtp(SendOtpRequest(email, name))
        call.enqueue(object : Callback<StatusResponse> {
            override fun onResponse(
                call: Call<StatusResponse>,
                response: Response<StatusResponse>
            ) {

                if (response.isSuccessful) {
                    ToastUtil.showCustomToast(
                        requireContext(),
                        "${response.body()?.message.toString()}",
                        true
                    )
                    //Toast.makeText(this@RegisterActivity, "${response.body()?.message.toString()}", Toast.LENGTH_SHORT).show()
                    showOtpDialog(email, name)
                }
            }

            override fun onFailure(call: Call<StatusResponse>, response: Throwable) {
                ToastUtil.showCustomToast(requireContext(), getString(R.string.network_request_failed_toast), false)
                //Toast.makeText(this@RegisterActivity, "Network request failed", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun updateUserProfile() {
        val firstName = binding?.editTextFirstName?.text.toString().trim()
        val lastName = binding?.editTextLastName?.text.toString().trim()
        val email = binding?.editTextUserEmail?.text.toString().trim()

        if (validateInput(firstName, lastName, email)) {
            val call = apiInterface?.editProfile(
                EditProfileRequest(
                    firstName, lastName,
                    userName!!, email
                )
            )
            call?.enqueue(object : Callback<EditProfileResponse> {
                override fun onResponse(
                    call: Call<EditProfileResponse>,
                    response: Response<EditProfileResponse>
                ) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        ToastUtil.showCustomToast(
                            requireContext(),
                            response.body()?.message ?: getString(R.string.profile_updated_successfully),
                            true
                        )
                        findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                    } else {
                        ToastUtil.showCustomToast(
                            requireContext(),
                            "Failed to update profile: ${response.message()}",
                            false
                        )
                    }
                }

                override fun onFailure(call: Call<EditProfileResponse>, t: Throwable) {
                    Log.e("EditProfileFragment", "Error: ${t.message}", t)
                    ToastUtil.showCustomToast(
                        requireContext(),
                        getString(R.string.network_request_failed_toast),
                        false
                    )
                }
            })
        }
    }

    private fun validateInput(
        firstName: String,
        lastName: String,
        email: String
    ): Boolean {
        var isValid = true

        if (firstName.isEmpty()) {
            binding?.textInputLayoutFirstName?.error =getString(R.string.first_name_is_required)
            isValid = false
        }else if(firstName.length>20){
            binding?.textInputLayoutFirstName?.error = getString(R.string.first_name_is_too_long)
            isValid = false
        }
        else {
            binding?.textInputLayoutFirstName?.error = null
        }

        if (lastName.isEmpty()) {
            binding?.textInputLayoutLastName?.error = getString(R.string.last_name_is_required)
            isValid = false
        } else if(lastName.length>20){
            binding?.textInputLayoutLastName?.error = getString(R.string.last_name_is_too_long)
            isValid = false
        } else {
            binding?.textInputLayoutLastName?.error = null
        }

        if (email.isEmpty()) {
            binding?.textInputLayoutUserEmail?.error = getString(R.string.email_is_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding?.textInputLayoutUserEmail?.error = getString(R.string.invalid_email_format)
            isValid = false
        } else {
            binding?.textInputLayoutUserEmail?.error = null
        }

        return isValid
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

    private fun showSaveProfileConfirmationDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.buttonYes.setOnClickListener {
            updateUserProfile()
            dialog.dismiss()
        }

        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addTextWatchers() {
        binding?.editTextFirstName?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkIfSaveEnabled()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding?.editTextLastName?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkIfSaveEnabled()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding?.editTextUserEmail?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkIfSaveEnabled()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun checkIfSaveEnabled() {
        val firstNameChanged = binding?.editTextFirstName?.text.toString() != initialFirstName
        val lastNameChanged = binding?.editTextLastName?.text.toString() != initialLastName
        val emailChanged = binding?.editTextUserEmail?.text.toString() != initialEmail

        // Enable Save button if at least one field is changed
        binding?.saveProfileButton?.isEnabled = firstNameChanged || lastNameChanged || emailChanged
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}