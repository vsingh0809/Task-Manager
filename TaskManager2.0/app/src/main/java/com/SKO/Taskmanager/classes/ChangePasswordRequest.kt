package com.SKO.Taskmanager.classes

data class ChangePasswordRequest(

   var oldPassword: String,

 var newPassword: String,

  var confirmPassword:String
)
