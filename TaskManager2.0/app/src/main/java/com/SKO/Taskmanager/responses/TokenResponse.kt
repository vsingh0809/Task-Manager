package com.SKO.Taskmanager.responses

data class TokenResponse (
    val accessToken: String ? = null,
    val refreshToken: String ? = null,
    var message : String ? = null
)