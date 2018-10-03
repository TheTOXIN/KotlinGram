package com.toxin.kotlingram.model

data class User(
        val name: String,
        val bio: String,
        val profilePicturePath: String?,
        val registrationTokens: MutableList<String>,
        val counter: MutableMap<String, Int> = mutableMapOf()
) {
    constructor() : this("", "", null, mutableListOf())
}