package com.example.rickandmortyapplication.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rickandmortyapplication.databinding.FragmentOnboardBinding

class OnboardFragment : Fragment() {
    private var _binding: FragmentOnboardBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("Non-zero value was expected")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.butAuth.setOnClickListener {
            val action = OnboardFragmentDirections.actionOnboardFragmentToSignInFragment(null)
            findNavController().navigate(action)
        }

        binding.butReg.setOnClickListener {
            val action = OnboardFragmentDirections.actionOnboardFragmentToSignUpFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}