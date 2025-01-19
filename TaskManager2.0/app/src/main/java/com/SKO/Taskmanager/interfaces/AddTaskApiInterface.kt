package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.classes.addTaskResponse
import com.SKO.Taskmanager.responses.AddTaskRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AddTaskApiInterface {
    @POST("api/Tasks")
    fun addTask(@Body taskData: AddTaskRequest): Call<addTaskResponse>

}