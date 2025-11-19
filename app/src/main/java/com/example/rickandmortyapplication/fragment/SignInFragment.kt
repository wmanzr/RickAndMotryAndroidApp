package com.example.rickandmortyapplication.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.rickandmortyapplication.data.SQLiteDB
import com.example.rickandmortyapplication.databinding.FragmentSignInBinding
import com.google.android.material.snackbar.Snackbar

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("Non-zero value was expected")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userArg = arguments?.let { SignInFragmentArgs.fromBundle(it).user }

        userArg?.let { user ->
            binding.headerAuth.text = "Добро пожаловать, ${user.login}!\n${user.email}"
            binding.userLoginAuth.setText(user.login)
        }

        binding.auth.setOnClickListener {
            val login = binding.userLoginAuth.text.toString().trim()
            val pass = binding.userPassAuth.text.toString().trim()

            if (login.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля!", Toast.LENGTH_LONG).show()
            } else {
                val db = SQLiteDB(requireContext(), null)
                val isAuth = db.getUSer(login, pass)
                if (isAuth) {
                    Snackbar.make(view, "Вы авторизованы", Snackbar.LENGTH_LONG).show()
                    binding.userLoginAuth.text.clear()
                    binding.userPassAuth.text.clear()

                    val action = SignInFragmentDirections.actionSignInFragmentToHomeFragment(user = userArg)
                    findNavController().navigate(action)
                } else {
                    Snackbar.make(view, "Неверные данные", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        binding.butReg.setOnClickListener {
            val action = SignInFragmentDirections.actionSignInFragmentToSignUpFragment()
            findNavController().navigate(action)
        }

        binding.backButtonAuth.setOnClickListener {
            val action = SignInFragmentDirections.actionSignInFragmentToOnboardFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}