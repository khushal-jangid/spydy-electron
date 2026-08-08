package com.spydy.reminder

data class Reminder(
    val id: String,
    val sender: String,
    val message: String,
    val triggerTimeMs: Long
)
