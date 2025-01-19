package com.SKO.Taskmanager.classes

import com.SKO.Taskmanager.responses.TaskDataClass

data class addTaskResponse(
    var message: String,
    var task: TaskDataClass
)
