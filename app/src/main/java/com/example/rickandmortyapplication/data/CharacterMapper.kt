package com.example.rickandmortyapplication.data

import com.example.rickandmortyapplication.model.Character
import com.example.rickandmortyapplication.model.Location
import com.example.rickandmortyapplication.model.Origin

fun Character.toEntity(): CharacterEntity {
    return CharacterEntity(
        id = this.id,
        name = this.name,
        status = this.status,
        species = this.species,
        type = this.type,
        gender = this.gender,
        originName = this.origin.name,
        originUrl = this.origin.url,
        locationName = this.location.name,
        locationUrl = this.location.url,
        image = this.image,
        episode = this.episode.joinToString(","),
        url = this.url,
        created = this.created
    )
}

fun CharacterEntity.toCharacter(): Character {
    return Character(
        id = this.id,
        name = this.name,
        status = this.status,
        species = this.species,
        type = this.type,
        gender = this.gender,
        origin = Origin(name = this.originName, url = this.originUrl),
        location = Location(name = this.locationName, url = this.locationUrl),
        image = this.image,
        episode = this.episode.split(",").filter { it.isNotEmpty() },
        url = this.url,
        created = this.created
    )
}

// Преобразование списка
fun List<Character>.toEntityList(): List<CharacterEntity> = this.map { it.toEntity() }
fun List<CharacterEntity>.toCharacterList(): List<Character> = this.map { it.toCharacter() }