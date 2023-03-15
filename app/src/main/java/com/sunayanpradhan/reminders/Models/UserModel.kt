package com.sunayanpradhan.reminders.Models

data class UserModel(var userId:String,
                     var userName:String,
                     var userEmail:String,
                     var userProfile:String
                     )
{
    constructor():this(
        "",
        "",
        "",
        ""
    )

}
