package com.SKO.Taskmanager.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.classes.ApiClient
import com.SKO.Taskmanager.databinding.ActivityLoginBinding
import com.SKO.Taskmanager.databinding.DialogForgotPasswordBinding
import com.SKO.Taskmanager.databinding.DialogResetPasswordBinding
import com.SKO.Taskmanager.databinding.DialogVerifyOtpBinding
import com.SKO.Taskmanager.interfaces.ApiInterface
import com.SKO.Taskmanager.interfaces.LoginApiInterface
import com.SKO.Taskmanager.interfaces.ResetPasswordApiInterface
import com.SKO.Taskmanager.interfaces.VerifyUserEmailApiInterface
import com.SKO.Taskmanager.responses.LoginUser
import com.SKO.Taskmanager.responses.ResetPassword
import com.SKO.Taskmanager.responses.SendOtpRequest
import com.SKO.Taskmanager.responses.StatusResponse
import com.SKO.Taskmanager.responses.TokenResponse
import com.SKO.Taskmanager.responses.VerifyEmailResponse
import com.SKO.Taskmanager.responses.VerifyOtpRequest
import com.SKO.Taskmanager.utils.SharedPreferencesUtil
import com.SKO.Taskmanager.utils.ToastUtil
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

class LoginActivity : AppCompatActivity() {

    private var loginBinding: ActivityLoginBinding?=null
    private var loginApiInterface: LoginApiInterface? = null
    private var verifyUserEmailApiInterface : VerifyUserEmailApiInterface? = null
    private var resetPasswordApiInterface : ResetPasswordApiInterface?= null
    private var progressDialog: ProgressDialog? = null
    private var forgotPasswordDialog: AlertDialog? = null  // Variable to keep track of forgot password dialog
    private var resetPasswordDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding?.root)

        loginBinding?.loginProgressBar?.visibility = View.GONE

        loginApiInterface = ApiClient.getMainClient(this).create(LoginApiInterface::class.java)
        verifyUserEmailApiInterface = ApiClient.getMainClient(this).create(VerifyUserEmailApiInterface::class.java)
        resetPasswordApiInterface = ApiClient.getMainClient(this).create(ResetPasswordApiInterface::class.java)
        loginBinding?.buttonLogin?.isEnabled=false
         val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                val email = loginBinding?.editTextLoginEmail?.text.toString().trim()
                val password = loginBinding?.editTextLoginPassword?.text.toString().trim()

                val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()
//                val isValidPassword = password.length >= 8
                val isValidPassword = password.length >= 8 && ValidationUtils.isValidPassword(password)

                loginBinding?.buttonLogin?.isEnabled = isValidEmail && isValidPassword
            }
        }

        loginBinding?.editTextLoginEmail?.addTextChangedListener(textWatcher)
        loginBinding?.editTextLoginPassword?.addTextChangedListener(textWatcher)

        loginBinding?.buttonLogin?.setOnClickListener {
            validateAndLogin()
        }

        loginBinding?.textViewLoginResgister?.setOnClickListener {
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }

        loginBinding?.textViewLoginForgotPassword?.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val dialogBinding = DialogForgotPasswordBinding.inflate(layoutInflater)

        forgotPasswordDialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        val emailEditText = dialogBinding.editTextEmailFP
        val sendOtpButton = dialogBinding.buttonDialogSendOtp

        sendOtpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                verifyUser(email)
            } else {
                ToastUtil.showCustomToast(this@LoginActivity, getString(R.string.enter_valid_email_toast), false)
            }
        }

        forgotPasswordDialog?.show()
    }

    private fun verifyUser(email: String) {
        loginBinding?.loginProgressBar?.visibility= View.VISIBLE

        val call: Call<VerifyEmailResponse>? = verifyUserEmailApiInterface?.verifyEmail(email)
        call?.enqueue(object : Callback<VerifyEmailResponse> {
            override fun onResponse(
                call: Call<VerifyEmailResponse>,
                response: Response<VerifyEmailResponse>
            ) {
                loginBinding?.loginProgressBar?.visibility = View.GONE

                if (response.isSuccessful) {

                    if (response.body()?.emailExists == false) {
                        ToastUtil.showCustomToast(
                            this@LoginActivity,
                            getString(R.string.email_not_registered_toast),
                            false
                        )

                    } else {
                        val call: Call<StatusResponse> ?=
                            verifyUserEmailApiInterface?.sendOtp(SendOtpRequest(email, email))
                        call?.enqueue(object : Callback<StatusResponse> {
                            override fun onResponse(
                                call: Call<StatusResponse>,
                                response: Response<StatusResponse>
                            ) {
                                if (response.isSuccessful) {
                                    ToastUtil.showCustomToast(
                                        this@LoginActivity,
                                        "${response.body()?.message.toString()}",
                                        true
                                    )

                                    showOtpDialog(email, email)
                                }
                            }

                            override fun onFailure(
                                call: Call<StatusResponse>,
                                response: Throwable
                            ) {
                                ToastUtil.showCustomToast(
                                    this@LoginActivity,
                                    getString(R.string.network_request_failed_toast),
                                    false
                                )
                            }

                        })
                    }
                } else {
                    ToastUtil.showCustomToast(
                        this@LoginActivity,
                        getString(R.string.network_request_failed_toast),
                        false
                    )
                }
            }

            override fun onFailure(call: Call<VerifyEmailResponse>, t: Throwable) {
                loginBinding?.loginProgressBar?.visibility = View.GONE
                ToastUtil.showCustomToast(
                    this@LoginActivity,
                    getString(R.string.network_request_failed_toast),
                    false
                )

            }
        })

    }


    private fun sendOtpToEmail(email: String) {
        loginBinding?.loginProgressBar?.visibility = View.VISIBLE


        val call : Call<StatusResponse> ? = verifyUserEmailApiInterface?.sendOtp(SendOtpRequest(email,email))

        call?.enqueue(object : Callback<StatusResponse> {

            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                loginBinding?.loginProgressBar?.visibility = View.GONE

                if (response.isSuccessful) {
                    Log.d("loginact",response.body().toString())
                    ToastUtil.showCustomToast(this@LoginActivity,"${response.body()?.message.toString()}",true)
                } else {
                    Log.d("loginact",response.body().toString())
                    ToastUtil.showCustomToast(this@LoginActivity,getString(R.string.failed_to_send_OTP_toast),false)
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                loginBinding?.loginProgressBar?.visibility = View.GONE

                ToastUtil.showCustomToast(this@LoginActivity,getString(R.string.network_error_toast),false)
            }
        })
    }

    private fun showOtpDialog(email: String, name: String) {
        val dialogBinding = DialogVerifyOtpBinding.inflate(LayoutInflater.from(this@LoginActivity))

        val otpDialog = AlertDialog.Builder(this@LoginActivity)
            .setView(dialogBinding.root)  // Use the root view of the ViewBinding
            .setPositiveButton("Resend Otp") { dialog, _ ->
                resendOtp(email,email)
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()


        dialogBinding.buttonVerifyOtp.setOnClickListener {
            val otp = dialogBinding.otpEditText.text.toString().trim()

            if (otp.isEmpty()) {
                ToastUtil.showCustomToast(this@LoginActivity, getString(R.string.OTP_is_required_toast), false)
            } else {
                verifyOtp(otp, email)
                otpDialog.dismiss()
                //showResetPasswordDialog(email)
            }

        }

        otpDialog.show()
    }

    private fun verifyOtp(otp: String, email: String) {
        loginBinding?.loginProgressBar?.visibility = View.VISIBLE


        val verifyOtpRequest = VerifyOtpRequest(email, otp)

        val call: Call<String>? = verifyUserEmailApiInterface?.verifyOtp(verifyOtpRequest)
        call?.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                loginBinding?.loginProgressBar?.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    // Success case
                    if (response.body().equals("OTP is valid.")) {
                        ToastUtil.showCustomToast(this@LoginActivity, getString(R.string.email_verified_successfully_toast), true)
                        showResetPasswordDialog(email)

                    } else {
                        ToastUtil.showCustomToast(this@LoginActivity, getString(R.string.invalid_OTP_toast), false)
                    }
                } else {
                    // Handle error case (plain string response)
                    val errorMessage = response.errorBody()?.string() ?: getString(R.string.Invalid_or_expired_OTP_default_messege) // Default message
                    ToastUtil.showCustomToast(this@LoginActivity, errorMessage, false)
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                loginBinding?.loginProgressBar?.visibility = View.GONE
                ToastUtil.showCustomToast(this@LoginActivity,getString(R.string.network_request_failed_toast), false)
            }
        })
    }



    private fun showResetPasswordDialog(email: String) {

        var isValid = true
        val dialogBinding = DialogResetPasswordBinding.inflate(layoutInflater)

        resetPasswordDialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.buttonResetPassword.setOnClickListener {
            val newPassword = dialogBinding.newPasswordEditText.text.toString().trim()
            val confirmPassword = dialogBinding.editTextDialogConfirmPassword.text.toString().trim()

            if (ValidationUtils.isEmpty(newPassword)) {
                dialogBinding?.textInputLayoutResetPassDialog?.error = getString(R.string.password_is_required_messege)
                dialogBinding?.newPasswordEditText?.requestFocus()
                isValid = false
            } else if (newPassword.length < 8) {
                dialogBinding?.textInputLayoutResetPassDialog?.error = getString(R.string.password_should_be_at_least_8_characters_messege)
                dialogBinding?.newPasswordEditText?.requestFocus()
                isValid = false
            } else if (!ValidationUtils.isValidPassword(newPassword)) {
                dialogBinding?.textInputLayoutResetPassDialog?.error = getString(R.string.password_should_be_alphanumeric_error)
                dialogBinding?.newPasswordEditText?.requestFocus()
                isValid = false
            } else {
                dialogBinding?.textInputLayoutResetPassDialog?.error = null
            }

            if (ValidationUtils.isEmpty(confirmPassword)) {
                dialogBinding?.textInputLayoutResetPassDialog2?.error = getString(R.string.please_confirm_your_password_messege)
                dialogBinding?.editTextDialogConfirmPassword?.requestFocus()
                isValid = false
            } else if (newPassword != confirmPassword) {
                dialogBinding?.textInputLayoutResetPassDialog2?.error = getString(R.string.passwords_do_not_match_messege)
                dialogBinding?.editTextDialogConfirmPassword?.requestFocus()
                isValid = false
            } else {
                dialogBinding?.textInputLayoutResetPassDialog2?.error = null
            }

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(editable: Editable?) {
                    isValid = true
                }
            }

            dialogBinding.buttonResetPassword.addTextChangedListener(textWatcher)
            dialogBinding.editTextDialogConfirmPassword.addTextChangedListener(textWatcher)

            if (isValid && newPassword == confirmPassword) {
                resetPassword(email, newPassword)
                resetPasswordDialog?.dismiss()
            }
//            else{
//                ToastUtil.showCustomToast(this@LoginActivity, getString(R.string.passwords_do_not_match_toast), false)
//            }
        }

        resetPasswordDialog?.show()
    }


    private fun resetPassword(email: String, newPassword: String) {

        loginBinding?.loginProgressBar?.visibility = View.VISIBLE


        val resetPass = ResetPassword(email, newPassword)

        val call = resetPasswordApiInterface?.resetPassword(resetPass)
        call?.enqueue(object : Callback<StatusResponse> {
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                loginBinding?.loginProgressBar?.visibility = View.GONE

                if (response.isSuccessful) {
                    ToastUtil.showCustomToast(this@LoginActivity,"${response.body()?.message.toString()}",true)
                    startActivity(Intent(this@LoginActivity, LoginActivity::class.java))
                } else {
                    ToastUtil.showCustomToast(this@LoginActivity,getString(R.string.failed_to_reset_password_toast),false)
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                loginBinding?.loginProgressBar?.visibility = View.GONE

                ToastUtil.showCustomToast(this@LoginActivity,getString(R.string.network_error_toast),false)
            }
        })

    }

    private fun resendOtp(email: String, name: String) {
        loginBinding?.loginProgressBar?.visibility = View.VISIBLE


        val call: Call<StatusResponse>? = verifyUserEmailApiInterface?.sendOtp(SendOtpRequest(email,email))
        call?.enqueue(object : Callback<StatusResponse> {
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                loginBinding?.loginProgressBar?.visibility = View.GONE

                if (response.isSuccessful) {
                    ToastUtil.showCustomToast(this@LoginActivity,"${response.body()?.message.toString()}", true)

                } else {
                    ToastUtil.showCustomToast(this@LoginActivity,getString(R.string.failed_to_resend_OTP_toast),false)
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                loginBinding?.loginProgressBar?.visibility = View.GONE

                ToastUtil.showCustomToast(this@LoginActivity,"Error: ${t.message}",false)
            }
        })
    }


    private fun validateAndLogin() {

        var loginEmail = loginBinding?.editTextLoginEmail?.text.toString().trim()
        var loginPassword = loginBinding?.editTextLoginPassword?.text.toString().trim()
        var isValid = true;

        if (ValidationUtils.isEmpty(loginEmail)) {
            loginBinding?.textInputLayoutLoginEmail?.error = getString(R.string.email_is_required_text)
            loginBinding?.editTextLoginEmail?.requestFocus()
            isValid = false

        } else if (!Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()) {
            loginBinding?.textInputLayoutLoginEmail?.error = getString(R.string.invalid_email_format_text)
            loginBinding?.editTextLoginEmail?.requestFocus()
            isValid = false
        } else {
            loginBinding?.textInputLayoutLoginEmail?.error = null
        }

        if (ValidationUtils.isEmpty(loginPassword)) {
            loginBinding?.textInputLayoutLoginPassword?.error = getString(R.string.password_is_required_messege)
            loginBinding?.editTextLoginPassword?.requestFocus()
            isValid = false

        } else if (loginPassword.length < 8) {
            loginBinding?.textInputLayoutLoginPassword?.error = getString(R.string.password_should_be_at_least_8_characters_messege)
            loginBinding?.editTextLoginPassword?.requestFocus()
            isValid = false
        } else {
            loginBinding?.textInputLayoutLoginPassword?.error = null
        }

        if(isValid){

            loginBinding?.loginProgressBar?.visibility = View.VISIBLE


            val loginUser = LoginUser(loginEmail, loginPassword)

            val call : Call<TokenResponse>? = loginApiInterface?.login(loginUser)

            call?.enqueue(object : Callback<TokenResponse>{
                override fun onResponse(p0: Call<TokenResponse>, p1: Response<TokenResponse>) {
                    loginBinding?.loginProgressBar?.visibility = View.GONE

                    if(p1.isSuccessful){
                        SharedPreferencesUtil.saveTokens(this@LoginActivity,p1.body()?.accessToken.toString(), p1.body()?.refreshToken.toString())
                        ToastUtil.showCustomToast(this@LoginActivity,getString(R.string.user_logged_in_successfully_toast),true)
                        showProgressDialog()

                        // Simulate a delay for fetching user data
                        android.os.Handler(Looper.getMainLooper()).postDelayed({
                            dismissProgressDialog()
                        }, 3000)
                        var intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    else{
                        val errorBody = p1.errorBody()?.string() // Get raw JSON error string
                        try {
                            val gson = Gson()
                            val errorResponse = gson.fromJson(errorBody, TokenResponse::class.java)
                            val errorMessage = errorResponse.message ?: getString(R.string.login_failed_default_error_messege) // Default message

                            ToastUtil.showCustomToast(this@LoginActivity, errorMessage, false)
                        } catch (e: Exception) {
                            ToastUtil.showCustomToast(this@LoginActivity, getString(R.string.an_error_occurred_toast), false)
                        }

                    }
                }

                override fun onFailure(p0: Call<TokenResponse>, p1: Throwable) {
                    loginBinding?.loginProgressBar?.visibility = View.GONE

                    ToastUtil.showCustomToast(this@LoginActivity,getString(R.string.network_error_toast),false)
                }

            })


        }
    }
    private fun showProgressDialog() {
        progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.fetching_user_tasks_text))
            setCancelable(false) // Prevent manual dismissal
            show()
        }
    }

    private fun dismissProgressDialog() {
//        progressDialog?.takeIf { it.isShowing }?.dismiss()
        if (!isFinishing && !isDestroyed && progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
        }
    }

    object ValidationUtils {

        fun isEmpty(input: String?): Boolean {
            return input.isNullOrBlank()
        }

        fun isValidPassword(password: String): Boolean {
//            val regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*\$"
            var regex = "^(?=.*\\d)(?=.*[a-zA-Z])(?=\\S*\$).*\$"
            return password.matches(regex.toRegex())
        }
    }

    override fun onBackPressed() {

        if (forgotPasswordDialog != null && forgotPasswordDialog?.isShowing == true) {
            forgotPasswordDialog?.dismiss()
        } else if (resetPasswordDialog != null && resetPasswordDialog?.isShowing == true) {
            resetPasswordDialog?.dismiss()
        } else {
            super.onBackPressed()
        }
    }
}