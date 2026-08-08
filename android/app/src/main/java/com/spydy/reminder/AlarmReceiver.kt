package com.spydy.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderText = intent.getStringExtra("EXTRA_REMINDER_TEXT") ?: "Time to drink water! 💧"

        val serviceIntent = Intent(context, OverlayService::class.java).apply {
            putExtra("EXTRA_REMINDER_TEXT", reminderText)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
