package com.SKO.Taskmanager.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.databinding.FragmentVerifyBinding

class VerifyFragment : Fragment() {

    private var binding : FragmentVerifyBinding ? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerifyBinding.inflate(inflater,container,false)

        return binding!!.root
    }
}