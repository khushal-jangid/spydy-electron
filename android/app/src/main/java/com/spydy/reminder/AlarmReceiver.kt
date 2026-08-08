package com.spydy.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("EXTRA_REMINDER_ID") ?: ""
        val sender = intent.getStringExtra("EXTRA_SENDER") ?: "Spider-Man 🕷️"
        val reminderText = intent.getStringExtra("EXTRA_REMINDER_TEXT") ?: "Time to drink water! 💧"
        val triggerTime = intent.getLongExtra("EXTRA_TRIGGER_TIME", System.currentTimeMillis())

        if (id.isNotEmpty()) {
            ReminderStore.remove(context, id)
        }

        val serviceIntent = Intent(context, OverlayService::class.java).apply {
            putExtra("EXTRA_REMINDER_ID", id)
            putExtra("EXTRA_SENDER", sender)
            putExtra("EXTRA_REMINDER_TEXT", reminderText)
            putExtra("EXTRA_TRIGGER_TIME", triggerTime)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
