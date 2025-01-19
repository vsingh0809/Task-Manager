package com.SKO.Taskmanager.responses

data class GetAllTaskResponse(
    var meassage: String? = null,
    var task: List<TaskDataClass>?=null
)
