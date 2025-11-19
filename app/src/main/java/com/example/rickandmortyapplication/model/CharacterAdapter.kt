package com.example.rickandmortyapplication.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.rickandmortyapplication.databinding.ItemCharacterBinding

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