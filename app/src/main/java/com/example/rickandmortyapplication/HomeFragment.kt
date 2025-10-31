package com.example.rickandmortyapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.rickandmortyapplication.databinding.FragmentHomeBinding
import com.example.rickandmortyapplication.databinding.ItemCharacterBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("Non-zero value was expected")
    private val repository = RickAndMortyRepository()
    private var page = 1
    private val characters = mutableListOf<Character>()
    private lateinit var adapter: CharacterAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CharacterAdapter(characters)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        loadCharacters(page)

        binding.nextPageButton.setOnClickListener {
            page++
            loadCharacters(page)
        }
    }

    private fun loadCharacters(page: Int) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE

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

    class CharacterAdapter(
        private val items: List<Character>
    ) : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

        class CharacterViewHolder(val binding: ItemCharacterBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
            val binding = ItemCharacterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return CharacterViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
            val character = items[position]
            holder.binding.characterName.text = character.name
            holder.binding.characterStatus.text = "${character.status} - ${character.species}"
            holder.binding.characterLocation.text = character.location.name

            Glide.with(holder.binding.root.context)
                .load(character.image)
                .transform(RoundedCorners(32))
                .into(holder.binding.characterImage)
        }

        override fun getItemCount() = items.size
    }
}