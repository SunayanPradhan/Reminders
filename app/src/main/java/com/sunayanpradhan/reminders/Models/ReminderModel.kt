package com.sunayanpradhan.reminders.Models

data class ReminderModel(var reminderId:String,var reminderTitle:String,var reminderDescription:String,var reminderDate:Long,var reminderCompleted:Boolean)
{
    constructor():this(
        "",
        "",
        "",
        0,
        false
    )
}
