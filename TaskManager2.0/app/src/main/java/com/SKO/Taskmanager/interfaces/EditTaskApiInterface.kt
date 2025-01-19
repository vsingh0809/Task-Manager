package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.responses.EditTaskRequest
import com.SKO.Taskmanager.responses.EditTaskResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT

interface EditTaskApiInterface {

    @PUT("api/Tasks")
    fun editTask(@Body taskData: EditTaskRequest): Call<EditTaskResponse>

}