package com.spydy.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {

    fun scheduleAlarm(context: Context, sender: String, text: String, minutesFromNow: Double, customId: String? = null): Reminder {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val id = customId ?: System.currentTimeMillis().toString()
        val triggerTimeMs = System.currentTimeMillis() + (minutesFromNow * 60 * 1000).toLong()

        val reminder = Reminder(
            id = id,
            sender = if (sender.isBlank()) "Spider-Man 🕷️" else sender,
            message = if (text.isBlank()) "Time to drink water! 💧" else text,
            triggerTimeMs = triggerTimeMs
        )

        ReminderStore.save(context, reminder)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_REMINDER_ID", reminder.id)
            putExtra("EXTRA_SENDER", reminder.sender)
            putExtra("EXTRA_REMINDER_TEXT", reminder.message)
            putExtra("EXTRA_TRIGGER_TIME", reminder.triggerTimeMs)
        }

        val requestCode = id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        }

        return reminder
    }

    fun cancelAlarm(context: Context, id: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        ReminderStore.remove(context, id)
    }

    fun rescheduleAll(context: Context) {
        val reminders = ReminderStore.getAll(context).filter { it.triggerTimeMs > System.currentTimeMillis() }
        reminders.forEach { r ->
            val remainingMins = (r.triggerTimeMs - System.currentTimeMillis()).toDouble() / (60 * 1000)
            if (remainingMins > 0) {
                scheduleAlarm(context, r.sender, r.message, remainingMins, r.id)
            }
        }
    }
}
