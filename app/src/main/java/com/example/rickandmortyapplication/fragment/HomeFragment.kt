package com.example.rickandmortyapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.rickandmortyapplication.model.Character
import com.example.rickandmortyapplication.model.CharacterAdapter
import com.example.rickandmortyapplication.data.DataStoreManager
import com.example.rickandmortyapplication.data.RickAndMortyRepository
import com.example.rickandmortyapplication.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("Non-zero value was expected")
    private val repository = RickAndMortyRepository()
    private var page = 1
    private var autoLoadEnabled = false
    private var displayMode = "list"
    private val characters = mutableListOf<Character>()
    private lateinit var adapter: CharacterAdapter
    private val autoLoadKey = booleanPreferencesKey("auto_load_pages")
    private val displayModeKey = stringPreferencesKey("display_mode")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            loadSettingsAndApply()
        }

        binding.nextPageButton.setOnClickListener {
            page++
            loadCharacters(page)
        }

        binding.buttonSettings.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
    }

    private suspend fun loadSettingsAndApply() {
        val dataStore = DataStoreManager.getDataStore(requireContext())
        val prefs = dataStore.data.first()

        autoLoadEnabled = prefs[autoLoadKey] ?: false
        displayMode = prefs[displayModeKey] ?: "list"

        adapter = CharacterAdapter(characters)

        binding.recyclerView.layoutManager = when (displayMode) {
            "grid" -> GridLayoutManager(requireContext(), 2)
            else -> LinearLayoutManager(requireContext())
        }

        binding.recyclerView.adapter = adapter

        binding.nextPageButton.visibility = if (autoLoadEnabled) View.GONE else View.VISIBLE
        if (autoLoadEnabled) {
            binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager
                    val lastVisiblePosition = when (layoutManager) {
                        is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
                        is GridLayoutManager -> layoutManager.findLastVisibleItemPosition()
                        else -> return
                    }

                    if (lastVisiblePosition >= characters.size - 3 && binding.progressBar.visibility != View.VISIBLE) {
                        page++
                        loadCharacters(page)
                    }
                }
            })
        }
        loadCharacters(page)
    }

    private fun loadCharacters(page: Int) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                if (!autoLoadEnabled) {
                    binding.recyclerView.visibility = View.GONE
                }

                val response = repository.getCharacters(page)

                when {
                    response.isSuccessful -> {
                        val body = response.body()
                        if (body != null) {
                            characters.addAll(body.results)
                            binding.recyclerView.adapter?.notifyDataSetChanged()
                        } else {
                            Toast.makeText(requireContext(), "Пустой ответ от сервера", Toast.LENGTH_SHORT).show()
                        }
                    }

                    response.code() == 404 -> {
                        Toast.makeText(requireContext(), "Страница не найдена", Toast.LENGTH_SHORT).show()
                    }

                    response.code() == 500 -> {
                        Toast.makeText(requireContext(), "Ошибка сервера", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        Toast.makeText(requireContext(), "Неизвестная ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}