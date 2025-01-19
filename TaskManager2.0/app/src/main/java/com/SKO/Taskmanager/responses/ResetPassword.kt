package com.SKO.Taskmanager.responses

data class ResetPassword (
    val Email : String?= null,
    val newPassword: String?=null
)