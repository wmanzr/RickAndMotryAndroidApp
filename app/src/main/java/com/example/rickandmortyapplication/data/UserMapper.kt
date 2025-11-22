package com.example.rickandmortyapplication.data

import com.example.rickandmortyapplication.model.User

fun User.toEntity(): UserEntity {
    return UserEntity(
        login = this.login,
        email = this.email,
        number = this.number,
        pass = this.pass
    )
}

fun UserEntity.toUser(): User {
    return User(
        login = this.login,
        email = this.email,
        number = this.number,
        pass = this.pass
    )
}