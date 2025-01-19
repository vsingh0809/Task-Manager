package com.SKO.Taskmanager.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.classes.ApiClient
import com.SKO.Taskmanager.classes.ChangePasswordRequest
import com.SKO.Taskmanager.databinding.DialogChangePasswordBinding
import com.SKO.Taskmanager.databinding.DialogEditProfileBinding
import com.SKO.Taskmanager.databinding.FragmentEditProfileChangePasswordBinding
import com.SKO.Taskmanager.interfaces.ApiInterface
import com.SKO.Taskmanager.responses.EditProfileResponse
import com.SKO.Taskmanager.utils.ToastUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileChangePasswordFragment : Fragment() {


    private var binding: FragmentEditProfileChangePasswordBinding? = null
    private var apiInterface: ApiInterface? = null
    private var toolbar : androidx.appcompat.widget.Toolbar ?= null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileChangePasswordBinding.inflate(inflater, container, false)
        apiInterface = ApiClient.getMainClient(requireContext()).create(ApiInterface::class.java)

        toolbar = binding?.toolbarInclude?.toolbar

        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

         binding?.savePasswordButton?.isEnabled=false
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.savePasswordButton?.setOnClickListener {
            showChangePasswordConfirmationDialog()
        }

        // Add live validation listeners
        binding?.editTextNewPass?.addTextChangedListener(createPasswordTextWatcher())
        binding?.editTextConformPass?.addTextChangedListener(createConfirmPasswordTextWatcher())

        binding?.resetPasswordButton?.setOnClickListener {
            binding?.editTextOldPass?.text?.clear()
            binding?.editTextNewPass?.text?.clear()
            binding?.editTextConformPass?.text?.clear()

        }
    }

    private fun changePassword() {
        val oldPassword = binding?.editTextOldPass?.text.toString().trim()
        val newPassword = binding?.editTextNewPass?.text.toString().trim()
        val confirmPassword = binding?.editTextConformPass?.text.toString().trim()

        if (validateInputs(oldPassword, newPassword, confirmPassword)) {
            val call = apiInterface?.changePassword(
                ChangePasswordRequest(
                    oldPassword,
                    newPassword,
                    confirmPassword
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
                            response.body()?.message ?: getString(R.string.password_changed_successfully),
                            true
                        )
                        findNavController().navigate(R.id.action_editProfileChangePasswordFragment_to_profileFragment)
                    } else {
                        ToastUtil.showCustomToast(
                            requireContext(),
                            response.body()?.message ?: getString(R.string.failed_to_change_password),
                            false
                        )
                    }
                }

                override fun onFailure(call: Call<EditProfileResponse>, t: Throwable) {
                    ToastUtil.showCustomToast(requireContext(), getString(R.string.network_error_toast), false)
                    Log.e("ChangePassword", "Error: ${t.message}", t)
                }
            })
        }
    }

    private fun validateInputs(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        if (oldPassword.isEmpty()) {
            binding?.editTextOldPass?.error = getString(R.string.old_password_is_required)
            binding?.textInputLayoutOldPass?.requestFocus()
            return false
        }

        if (newPassword.isEmpty()) {
            binding?.editTextNewPass?.error = getString(R.string.new_password_is_required)
            binding?.textInputLayoutNewPass?.requestFocus()
            return false
        }
        if (newPassword.length < 8) {
            binding?.textInputLayoutNewPass?.error =
                getString(R.string.password_must_be_at_least_8_characters_long)
            binding?.editTextNewPass?.requestFocus()
            return false
        }


        if (!newPassword.matches(Regex("^(?=.*\\d)(?=.*[a-zA-Z])(?=\\S*\$).*\$"))) {
            binding?.editTextNewPass?.error = getString(R.string.password_must_be_alphanumeric)
            binding?.textInputLayoutNewPass?.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding?.editTextConformPass?.error =   getString(R.string.confirm_password_is_required)
            binding?.textInputLayoutConformPass?.requestFocus()
            return false
        }

        if (newPassword != confirmPassword) {
            binding?.textInputLayoutConformPass?.error = getString(R.string.passwords_do_not_match)
            binding?.textInputLayoutConformPass?.requestFocus()
            return false
        }

        return true
    }

    private fun createPasswordTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val newPassword = s.toString().trim()

                when {
                    newPassword.isEmpty() -> {
                        binding?.editTextNewPass?.error = getString(R.string.new_password_)
                    }

                    newPassword.length < 8 -> {
                        binding?.editTextNewPass?.error =
                            getString(R.string.password)
                    }

                    !newPassword.matches(Regex("^(?=.*\\d)(?=.*[a-zA-Z])(?=\\S*\$).*\$")) -> {
                        binding?.editTextNewPass?.error =
                            getString(R.string.password_must_be_alphanumeric)
                    }

                    else -> {
                        binding?.editTextNewPass?.error = null
                    }
                }
            }

        }
    }

    private fun createConfirmPasswordTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString().trim()
                val newPassword = binding?.editTextNewPass?.text.toString().trim()
              binding?.savePasswordButton?.isEnabled=true
                if (confirmPassword.isEmpty()) {
                    binding?.editTextConformPass?.error =
                        getString(R.string.confirm_password_is_required)
                    binding?.savePasswordButton?.isEnabled=false
                } else if (newPassword != confirmPassword) {
                    binding?.editTextConformPass?.error = getString(R.string.passwords_do_not_match)
                    binding?.savePasswordButton?.isEnabled=false
                } else {
                    binding?.editTextConformPass?.error = null
                }
            }
        }
    }

    private fun showChangePasswordConfirmationDialog() {
        val dialogBinding = DialogChangePasswordBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.buttonYes.setOnClickListener {
            changePassword()
            dialog.dismiss()
        }

        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}