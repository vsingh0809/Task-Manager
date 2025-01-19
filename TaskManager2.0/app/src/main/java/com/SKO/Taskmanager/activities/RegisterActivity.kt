package com.SKO.Taskmanager.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.icu.text.DateTimePatternGenerator.PatternInfo.OK
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.classes.ApiClient
import com.SKO.Taskmanager.databinding.ActivityRegisterBinding
import com.SKO.Taskmanager.databinding.DialogVerifyOtpBinding
import com.SKO.Taskmanager.interfaces.ApiInterface
import com.SKO.Taskmanager.interfaces.RegisterApiInterface
import com.SKO.Taskmanager.interfaces.VerifyUserEmailApiInterface
import com.SKO.Taskmanager.responses.RegisterUser
import com.SKO.Taskmanager.responses.SendOtpRequest
import com.SKO.Taskmanager.responses.StatusResponse
import com.SKO.Taskmanager.responses.TokenResponse
import com.SKO.Taskmanager.responses.VerifyEmailRequest
import com.SKO.Taskmanager.responses.VerifyEmailResponse
import com.SKO.Taskmanager.responses.VerifyOtpRequest
import com.SKO.Taskmanager.utils.ToastUtil
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    //private var apiInterface: ApiInterface?=null
    private var registerApiInterface : RegisterApiInterface ?= null
    private var verifyUserEmailApiInterface : VerifyUserEmailApiInterface ? = null

    var isValid = true
    private var isEmailVerified = false

    private var registerBinding: ActivityRegisterBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(registerBinding?.root)

       // apiInterface = ApiClient.getMainClient(this@RegisterActivity).create(ApiInterface::class.java)
        registerApiInterface = ApiClient.getMainClient(this@RegisterActivity).create(RegisterApiInterface::class.java)
        verifyUserEmailApiInterface = ApiClient.getMainClient(this@RegisterActivity).create(VerifyUserEmailApiInterface::class.java)

        registerBinding?.buttonRegister?.isEnabled = true


        registerBinding?.editTextRegisterFirstname?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateUsername()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        registerBinding?.editTextRegisterLastName?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateUsername()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        registerBinding?.textViewRegisterVerifyEmail?.setOnClickListener {
            verifyEmail()
           // isEmailVerified = true
        }

        registerBinding?.buttonRegister?.setOnClickListener {
            if (!isEmailVerified) {
                //Toast.makeText(this, "Please verify your email", Toast.LENGTH_SHORT).show()
                ToastUtil.showCustomToast(this@RegisterActivity, getString(R.string.please_verify_your_email_toast), false)
                return@setOnClickListener
            }

            validateAndRegister()
        }

    }


    private fun verifyEmail() {
        val email = registerBinding?.editTextRegisterEmail?.text.toString().trim()
        val name = registerBinding?.editTextRegisterFirstname?.text.toString().trim()

        if (ValidationUtils.isEmpty(name)) {
            registerBinding?.textInputLayoutFirstName?.error = getString(R.string.first_name_is_required_text)
            registerBinding?.editTextRegisterFirstname?.requestFocus()
            isValid = false
        } else {
            registerBinding?.textInputLayoutFirstName?.error = null
        }

        if (ValidationUtils.isEmpty(email)) {
            registerBinding?.textInputLayoutEmail?.error = getString(R.string.Email_is_required_text)
            registerBinding?.editTextRegisterEmail?.requestFocus()
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registerBinding?.textInputLayoutEmail?.error = getString(R.string.invalid_email_format_text)
            registerBinding?.editTextRegisterEmail?.requestFocus()
            isValid = false
        } else {
            registerBinding?.textInputLayoutEmail?.error = null
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
               isValid=true
            }
        }

        registerBinding?.editTextRegisterFirstname?.addTextChangedListener(textWatcher)
        registerBinding?.editTextRegisterEmail?.addTextChangedListener(textWatcher)


        if(isValid) {
            registerBinding?.progressBar?.visibility = View.VISIBLE

            val verifyEmailRequest = VerifyEmailRequest(email)

            val call: Call<VerifyEmailResponse>? = verifyUserEmailApiInterface?.verifyEmail(email)
            call?.enqueue(object : Callback<VerifyEmailResponse> {
                override fun onResponse(
                    call: Call<VerifyEmailResponse>,
                    response: Response<VerifyEmailResponse>
                ) {
                    registerBinding?.progressBar?.visibility = View.GONE

                    if (response.isSuccessful) {
                        //val statusResponse = response.body()?.Data

                        if (response.body()?.emailExists == true) {
                            // User already exists, show a message and redirect to login
                            ToastUtil.showCustomToast(
                                this@RegisterActivity,
                                getString(R.string.user_already_exists_toast),
                                true
                            )

                            //Toast.makeText(this@RegisterActivity, "User already exists, redirecting to login...", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Email doesn't exist, send otp request and show OTP dialog
                            val call: Call<StatusResponse> =
                                verifyUserEmailApiInterface!!.sendOtp(SendOtpRequest(email, name))
                            call?.enqueue(object : Callback<StatusResponse> {
                                override fun onResponse(
                                    call: Call<StatusResponse>,
                                    response: Response<StatusResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        ToastUtil.showCustomToast(
                                            this@RegisterActivity,
                                            "${response.body()?.message.toString()}",
                                            true
                                        )

                                        // Toast.makeText(this@RegisterActivity, "${response.body()?.message.toString()}", Toast.LENGTH_LONG).show()
                                        showOtpDialog(email, name)
                                        isValid = true

                                    }
                                }

                                override fun onFailure(
                                    call: Call<StatusResponse>,
                                    response: Throwable
                                ) {
                                    ToastUtil.showCustomToast(
                                        this@RegisterActivity,
                                        getString(R.string.network_request_failed_toast),
                                        false
                                    )
                                    //Toast.makeText(this@RegisterActivity, "Some error occured", Toast.LENGTH_SHORT).show()
                                    isValid = false
                                }

                            })
                        }
                    } else {
                        ToastUtil.showCustomToast(
                            this@RegisterActivity,
                            getString(R.string.network_request_failed_toast),
                            false
                        )
                        // Toast.makeText(this@RegisterActivity, "Network request failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<VerifyEmailResponse>, t: Throwable) {
                    registerBinding?.progressBar?.visibility = View.GONE
//                Toast.makeText(this@RegisterActivity, "Network request failed", Toast.LENGTH_SHORT).show()
                    ToastUtil.showCustomToast(
                        this@RegisterActivity,
                        getString(R.string.network_request_failed_toast),
                        false
                    )

                }
            })
        }
    }



    private fun showOtpDialog(email: String, name: String) {
        val dialogBinding = DialogVerifyOtpBinding.inflate(LayoutInflater.from(this@RegisterActivity))

        val otpDialog = AlertDialog.Builder(this@RegisterActivity)
            .setView(dialogBinding.root)  // Use the root view of the ViewBinding
            .setPositiveButton("Resend Otp") { dialog, _ ->
                resendOtp(email,name)
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()


        dialogBinding.buttonVerifyOtp.setOnClickListener {
            val otp = dialogBinding.otpEditText.text.toString().trim()

            if (otp.isEmpty()) {
//                Toast.makeText(this@RegisterActivity, "OTP is required", Toast.LENGTH_SHORT).show()
                ToastUtil.showCustomToast(this@RegisterActivity, getString(R.string.OTP_is_required_toast), false)


            } else {
                verifyOtp(otp, email)
                otpDialog.dismiss()
            }

        }

        // Show the dialog
        otpDialog.show()
    }



    private fun verifyOtp(otp: String, email: String) {
        registerBinding?.progressBar?.visibility = View.VISIBLE

        val verifyOtpRequest = VerifyOtpRequest(email, otp)

        val call: Call<String>? = verifyUserEmailApiInterface?.verifyOtp(verifyOtpRequest)
        call?.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                registerBinding?.progressBar?.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    // Success case
                    if (response.body().equals(getString(R.string.OTP_is_valid_responce))) {
                        ToastUtil.showCustomToast(this@RegisterActivity, getString(R.string.email_verified_successfully_toast), true)
                        isEmailVerified = true

                    } else {
                        ToastUtil.showCustomToast(this@RegisterActivity, getString(R.string.invalid_OTP_toast), false)
                    }
                } else {
                    // Handle error case (plain string response)
                    val errorMessage = response.errorBody()?.string() ?: getString(R.string.Invalid_or_expired_OTP_default_messege) // Default message
                    ToastUtil.showCustomToast(this@RegisterActivity, errorMessage, false)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                registerBinding?.progressBar?.visibility = View.GONE
                //Toast.makeText(this@RegisterActivity, "Network request failed", Toast.LENGTH_SHORT).show()
                ToastUtil.showCustomToast(this@RegisterActivity, getString(R.string.network_request_failed_toast), false)
            }
        })
    }

    private fun resendOtp(email: String, name: String) {

        registerBinding?.progressBar?.visibility = View.VISIBLE

        val call : Call<StatusResponse> = verifyUserEmailApiInterface!!.sendOtp(SendOtpRequest(email,name))
        call?.enqueue(object : Callback<StatusResponse>{
            override fun onResponse(
                call: Call<StatusResponse>,
                response: Response<StatusResponse>
            ) {
                registerBinding?.progressBar?.visibility = View.GONE

                if(response.isSuccessful){
                    ToastUtil.showCustomToast(this@RegisterActivity, "${response.body()?.message.toString()}", true)
                    //Toast.makeText(this@RegisterActivity, "${response.body()?.message.toString()}", Toast.LENGTH_SHORT).show()
                    showOtpDialog(email, name)
                }
            }

            override fun onFailure(call: Call<StatusResponse>, response: Throwable) {
                registerBinding?.progressBar?.visibility = View.GONE
                ToastUtil.showCustomToast(this@RegisterActivity, getString(R.string.network_request_failed_toast), false)
                //Toast.makeText(this@RegisterActivity, "Network request failed", Toast.LENGTH_SHORT).show()
            }

        })
    }


    private fun updateUsername() {
        val firstname = registerBinding?.editTextRegisterFirstname?.text.toString().trim()
        val lastname = registerBinding?.editTextRegisterLastName?.text.toString().trim()
        // Generate the username by combining first name and last name
        val suggestedUsername = firstname + lastname
        registerBinding?.editTextRegisterUsername?.setText(suggestedUsername)
    }


    private fun validateAndRegister() {
        val firstname = registerBinding?.editTextRegisterFirstname?.text.toString().trim()
        val lastname = registerBinding?.editTextRegisterLastName?.text.toString().trim()
        val username = registerBinding?.editTextRegisterUsername?.text.toString().trim()
        val email = registerBinding?.editTextRegisterEmail?.text.toString().trim()
        val password = registerBinding?.editTextRegisterPassword?.text.toString().trim()
        val confirmPassword = registerBinding?.editTextRegisterConfirmPassword?.text.toString().trim()

        var isValid = true

        if (ValidationUtils.isEmpty(firstname)) {
            registerBinding?.textInputLayoutFirstName?.error = getString(R.string.first_name_is_required_text)
            registerBinding?.editTextRegisterFirstname?.requestFocus()
            isValid = false
        } else {
            registerBinding?.textInputLayoutFirstName?.error = null
        }

        if (ValidationUtils.isEmpty(lastname)) {
            registerBinding?.textInputLayoutLastName?.error = getString(R.string.last_name_is_required_text)
            registerBinding?.editTextRegisterLastName?.requestFocus()
            isValid = false
        } else {
            registerBinding?.textInputLayoutLastName?.error = null
        }

        if (ValidationUtils.isEmpty(username)) {
            registerBinding?.textInputLayoutUsername?.error = getString(R.string.username_is_required_text)
            registerBinding?.editTextRegisterUsername?.requestFocus()
            isValid = false
        } else {
            registerBinding?.textInputLayoutUsername?.error = null
        }

        if (ValidationUtils.isEmpty(email)) {
            registerBinding?.textInputLayoutEmail?.error = getString(R.string.Email_is_required_text)
            registerBinding?.editTextRegisterEmail?.requestFocus()
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registerBinding?.textInputLayoutEmail?.error = getString(R.string.invalid_email_format_text)
            registerBinding?.editTextRegisterEmail?.requestFocus()
            isValid = false
        } else {
            registerBinding?.textInputLayoutEmail?.error = null
        }

        if (ValidationUtils.isEmpty(password)) {
            registerBinding?.textInputLayoutPassword?.error = getString(R.string.password_is_required_messege)
            registerBinding?.editTextRegisterPassword?.requestFocus()
            isValid = false
        } else if (password.length < 8) {
            registerBinding?.textInputLayoutPassword?.error = getString(R.string.password_should_be_at_least_8_characters_messege)
            registerBinding?.editTextRegisterPassword?.requestFocus()
            isValid = false
        } else if(!ValidationUtils.isValidPassword(password)){
            registerBinding?.textInputLayoutPassword?.error = getString(R.string.password_should_be_alphanumeric_error)
            registerBinding?.editTextRegisterPassword?.requestFocus()
            isValid = false
        } else {
            registerBinding?.textInputLayoutPassword?.error = null
        }

        if (ValidationUtils.isEmpty(confirmPassword)) {
            registerBinding?.textInputLayoutConfirmPassword?.error = getString(R.string.please_confirm_your_password_messege)
            registerBinding?.editTextRegisterConfirmPassword?.requestFocus()
            isValid = false
        } else if (confirmPassword != password) {
            registerBinding?.textInputLayoutConfirmPassword?.error = getString(R.string.passwords_do_not_match_messege)
            registerBinding?.editTextRegisterConfirmPassword?.requestFocus()
            isValid = false
        } else {
            registerBinding?.textInputLayoutConfirmPassword?.error = null
        }


        if (isValid) {
            registerBinding?.progressBar?.visibility = View.VISIBLE
            //registerBinding?.buttonRegister?.isEnabled = false


            val registerUser = RegisterUser(firstname, lastname,  username, email, password)

            val call : Call<StatusResponse>? = registerApiInterface?.registerUser(registerUser)

            call?.enqueue(object : Callback<StatusResponse>{
                override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                    registerBinding?.progressBar?.visibility = View.GONE

                    if (response.isSuccessful){
                        ToastUtil.showCustomToast(this@RegisterActivity, " ${response.body()?.message}", true)

                        //Toast.makeText(this@RegisterActivity, "${response.body()?.message.toString()}", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        //ToastUtil.showCustomToast(this@RegisterActivity, " ${response.body()?.message}", false)

                        //Toast.makeText(this@RegisterActivity, "Some error occured in calling the api", Toast.LENGTH_LONG ).show()
                        val errorBody = response.errorBody()?.string() // Get raw JSON error string
                        try {
                            // Parse the error JSON into TokenResponse
                            val gson = Gson()
                            val errorResponse = gson.fromJson(errorBody, TokenResponse::class.java)
                            val errorMessage = errorResponse.message ?: getString(R.string.registration_failed_text) // Default message

                            // Show error message in toast
                            ToastUtil.showCustomToast(this@RegisterActivity, errorMessage, false)
                        } catch (e: Exception) {
                            // Handle parsing errors
                            ToastUtil.showCustomToast(this@RegisterActivity, getString(R.string.an_error_occurred_toast), false)
                        }
                    }
                }

                override fun onFailure(p0: Call<StatusResponse>, p1: Throwable) {
                    registerBinding?.progressBar?.visibility = View.GONE
//                    Toast.makeText(this@RegisterActivity, "Some error occurred in calling the API", Toast.LENGTH_SHORT).show()
                    ToastUtil.showCustomToast(this@RegisterActivity, getString(R.string.some_error_occurred_in_calling_the_API_toast), false)

                }

            })

        }
//        else {
//            textViewWarning.visibility = View.VISIBLE
//        }
    }




    object ValidationUtils {

        // Check if a string is empty or null
        fun isEmpty(input: String?): Boolean {
            return input.isNullOrBlank()
        }

        // Check if password meets basic complexity requirements
        fun isValidPassword(password: String): Boolean {
//            val regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*\$"
            var regex = "^(?=.*\\d)(?=.*[a-zA-Z])(?=\\S*\$).*\$"
            return password.matches(regex.toRegex())
        }
    }


}