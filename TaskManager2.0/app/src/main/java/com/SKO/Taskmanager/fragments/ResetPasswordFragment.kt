package com.SKO.Taskmanager.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.activities.LoginActivity
import com.SKO.Taskmanager.databinding.FragmentResetPasswordBinding
import com.SKO.Taskmanager.utils.ToastUtil

class ResetPasswordFragment : Fragment() {

    private var binding : FragmentResetPasswordBinding ? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentResetPasswordBinding.inflate(inflater,container,false)

        binding!!.buttonResetPassword.setOnClickListener {
            // fragment to activity
//            Toast.makeText(context, "Password changed successfully!!!", Toast.LENGTH_LONG).show()
            ToastUtil.showCustomToast(requireContext(), getString(R.string.password_changed_successfully_text), true)
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return binding!!.root
    }
}