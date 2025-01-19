package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.classes.addTaskResponse
import com.SKO.Taskmanager.responses.AddTaskRequest
import com.SKO.Taskmanager.responses.ApiResponse
import com.SKO.Taskmanager.responses.EditTaskRequest
import com.SKO.Taskmanager.responses.EditTaskResponse
import com.SKO.Taskmanager.responses.GetAllTaskResponse
import com.SKO.Taskmanager.responses.GetTasksByStartDateRequest
import com.SKO.Taskmanager.responses.MultiselectDeleteRequest
import com.SKO.Taskmanager.responses.StatusResponse
import com.SKO.Taskmanager.responses.TaskDataClass

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApiInterface {

    @POST("byStartDate")
    fun getTaskBYStartDate(@Body getTasksByStartDateRequest: GetTasksByStartDateRequest) : Call<StatusResponse>

    @PUT("api/Tasks/soft-delete/{taskId}")
    fun deleteTask(@Path("taskId") taskId: Int) : Call<StatusResponse>

    @PUT("api/Tasks")
    fun editTask(@Body taskData: EditTaskRequest): Call<EditTaskResponse>

    @GET("api/Tasks")
    fun getAllTasks() : Call<List<TaskDataClass>>

    @GET("api/Tasks/{taskId}")
    fun getByTaskId(@Path("taskId") taskId: Int): Call<ApiResponse>
    @POST("api/Tasks")
    fun addTask(@Body taskData: AddTaskRequest): Call<addTaskResponse>

    @GET("api/Tasks/byuser")
    fun getAllTasksByToken(): Call<GetAllTaskResponse>

    @PUT("api/Tasks/multi-delete")
    fun deleteMultipleTasks(@Body multiselectDeleteRequest: MultiselectDeleteRequest) : Call<StatusResponse>

}