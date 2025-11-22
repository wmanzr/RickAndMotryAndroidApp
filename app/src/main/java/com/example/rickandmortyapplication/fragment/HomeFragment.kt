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

import android.content.Context
import retrofit2.Response

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("Non-zero value was expected")
    private lateinit var repository: RickAndMortyRepository
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
        repository = RickAndMortyRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            loadSettingsAndApply()
            coldStart()
            observeDatabase()
        }

        binding.nextPageButton.setOnClickListener {
            page++
            loadCharactersFromApi(page, append = true)
        }

        binding.refreshButton.setOnClickListener {
                refreshCharacters()
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

        if (autoLoadEnabled) enableAutoPaging()
    }

    private fun enableAutoPaging() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)

                val lm = rv.layoutManager
                val lastVisible = when (lm) {
                    is LinearLayoutManager -> lm.findLastVisibleItemPosition()
                    is GridLayoutManager -> lm.findLastVisibleItemPosition()
                    else -> return
                }

                if (lastVisible >= characters.size - 3 &&
                    binding.progressBar.visibility != View.VISIBLE
                ) {
                    page++
                    loadCharactersFromApi(page, append = true)
                }
            }
        })
    }

    private fun coldStart() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val count = repository.getCharactersCount()
//                repository.deleteAllCharactersFromDb()
                if (count == 0) {
                    Toast.makeText(requireContext(), "Данные загружаются с сервера", Toast.LENGTH_SHORT).show()
                    loadCharactersFromApi(page, append = false)
                } else {
                    Toast.makeText(requireContext(), "Данные загружаются из базы данных", Toast.LENGTH_SHORT).show()
                    val dbCharacters = repository.getAllCharactersFromDb()
                    characters.clear()
                    characters.addAll(dbCharacters)
                    adapter.notifyDataSetChanged()

                    page = (count + 19) / 20
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun refreshCharacters() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Обновление...", Toast.LENGTH_SHORT).show()

                repository.deleteAllCharactersFromDb()
                characters.clear()
                for (i in 1..page) {
                    val response = repository.getCharacters(i)

                    response.handleResponse(requireContext()) { characterResponse ->
                        val newCharacters = characterResponse.results
                        repository.insertCharactersToDb(newCharacters)

                        characters.addAll(newCharacters)
                        adapter.notifyDataSetChanged()
                    }
                    Toast.makeText(requireContext(), "Обновлено!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка обновления: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadCharactersFromApi(page: Int, append: Boolean) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                if (!autoLoadEnabled && !append) {
                    binding.recyclerView.visibility = View.GONE
                }
                val response = repository.getCharacters(page)

                response.handleResponse(requireContext()) { characterResponse ->
                    val newCharacters = characterResponse.results
                    repository.insertCharactersToDb(newCharacters)

                    if (append) {
                        characters.addAll(newCharacters)
                    } else {
                        characters.clear()
                        characters.addAll(newCharacters)
                    }
                    adapter.notifyDataSetChanged()
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

    private fun observeDatabase() {
        lifecycleScope.launch {
            repository.getAllCharactersFlow()?.collect { dbCharacters ->
                if (dbCharacters != characters) {
                    characters.clear()
                    characters.addAll(dbCharacters)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    suspend fun <T> Response<T>.handleResponse(
        context: Context,
        onSuccess: suspend (T) -> Unit
    ): Boolean {
        return when {
            this.isSuccessful -> {
                val body = this.body()
                if (body != null) {
                    onSuccess(body)
                    true
                } else {
                    Toast.makeText(context, "Пустой ответ от сервера", Toast.LENGTH_SHORT).show()
                    false
                }
            }

            this.code() == 404 -> {
                Toast.makeText(context, "Страница не найдена", Toast.LENGTH_SHORT).show()
                false
            }

            this.code() == 500 -> {
                Toast.makeText(context, "Ошибка сервера", Toast.LENGTH_SHORT).show()
                false
            }

            else -> {
                Toast.makeText(context, "Неизвестная ошибка: ${this.code()}", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}