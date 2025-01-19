package com.SKO.Taskmanager.responses

data class RegisterUser(
    var FirstName : String?= null,
    var LastName : String?=null,
    var UserName : String?=null,
    var Email : String?= null,
    var Password : String?= null,
)