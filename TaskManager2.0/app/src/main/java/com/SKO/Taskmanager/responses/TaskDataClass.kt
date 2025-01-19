package com.SKO.Taskmanager.responses

data class TaskDataClass(
    var taskId: Int,
    var userId: Int,
    var title: String,
    var description: String,
    var status: String,
    var startDate: String?,
    var endDate: String?
)

data class ApiResponse(
    val message: String,
    val task: TaskDataClass
)
