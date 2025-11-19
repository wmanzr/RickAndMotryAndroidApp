package com.example.rickandmortyapplication.fragment

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.rickandmortyapplication.data.SQLiteDB
import com.example.rickandmortyapplication.model.User
import com.example.rickandmortyapplication.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("Non-zero value was expected")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reg.setOnClickListener {
            val login = binding.userLogin.text.toString().trim()
            val email = binding.userEmail.text.toString().trim()
            val number = binding.userNumber.text.toString().trim()
            val pass = binding.userPass.text.toString().trim()

            when {
                login.isEmpty() -> binding.userLogin.error = "Введите логин"
                login.length < 3 -> binding.userLogin.error = "Логин слишком короткий (мин. 3 символа)"
                !login.matches(Regex("^[a-zA-Z0-9_]+$")) -> binding.userLogin.error = "Логин может содержать только буквы, цифры и символ _"

                email.isEmpty() -> binding.userEmail.error = "Введите почту"
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.userEmail.error = "Неверный формат почты"

                number.isEmpty() -> binding.userNumber.error = "Введите номер телефона"
                !number.matches(Regex("^[0-9]{10,}$")) -> binding.userNumber.error = "Номер должен содержать минимум 10 цифр"

                pass.isEmpty() -> binding.userPass.error = "Введите пароль"
                pass.length < 6 -> binding.userPass.error = "Пароль слишком короткий (мин. 6 символов)"
                !pass.matches(Regex(".*[0-9].*")) -> binding.userPass.error = "Пароль должен содержать хотя бы одну цифру"
                !pass.matches(Regex(".*[@#\$%^&+=!].*")) -> binding.userPass.error = "Пароль должен содержать хотя бы один спецсимвол (@#\$%^&+=!)"

                else -> {
                    val user = User(login, email, number, pass)
                    val db = SQLiteDB(requireContext(), null)
                    db.addUSer(user)

                    Snackbar.make(view, "Вы зарегистрированы", Snackbar.LENGTH_LONG).show()

                    binding.userLogin.text.clear()
                    binding.userEmail.text.clear()
                    binding.userNumber.text.clear()
                    binding.userPass.text.clear()

                    val action = SignUpFragmentDirections.actionSignUpFragmentToSignInFragment(user)
                    view.findNavController().navigate(action)
                }
            }
        }

        binding.butAuth.setOnClickListener {
            val action = SignUpFragmentDirections.actionSignUpFragmentToSignInFragment(null)
            view.findNavController().navigate(action)
        }

        binding.backButtonReg.setOnClickListener {
            val action = SignUpFragmentDirections.actionSignUpFragmentToOnboardFragment()
            view.findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}