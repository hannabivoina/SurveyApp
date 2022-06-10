package com.example.myapplication.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.common.contract
import com.example.myapplication.databinding.FragmentStartBinding

class StartFragment: Fragment() {

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(inflater, container, false)

        binding.startGoOnlineButton.setOnClickListener {
            val userName = binding.startNameEditText.text.toString()
            contract()?.goOnline(userName)
            contract()?.goToSurveyRooms()
        }

        return binding.root
    }
}