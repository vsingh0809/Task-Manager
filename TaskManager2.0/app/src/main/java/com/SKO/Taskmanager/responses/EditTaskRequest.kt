package com.SKO.Taskmanager.responses

data class EditTaskRequest(

    val taskId: Int,
    val title: String,
    val description: String,
    val status: String,
    val endDate: String
)

