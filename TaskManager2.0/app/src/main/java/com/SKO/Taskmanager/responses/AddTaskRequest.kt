package com.SKO.Taskmanager.responses

data class AddTaskRequest(
    var title: String,
    var description: String,
    var endDate: String
)
