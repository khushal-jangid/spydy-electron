package com.spydy.reminder

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var etSender: EditText
    private lateinit var etMessage: EditText
    private lateinit var etMinutes: EditText
    private lateinit var btnSetCustom: Button
    private lateinit var btnTestDrop: Button
    private lateinit var containerActiveReminders: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            renderReminders()
            handler.postDelayed(this, 30000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()

        etSender = findViewById(R.id.etSender)
        etMessage = findViewById(R.id.etMessage)
        etMinutes = findViewById(R.id.etMinutes)
        btnSetCustom = findViewById(R.id.btnSetCustom)
        btnTestDrop = findViewById(R.id.btnTestDrop)
        containerActiveReminders = findViewById(R.id.containerActiveReminders)

        btnSetCustom.setOnClickListener {
            val sender = etSender.text.toString().trim()
            val msg = etMessage.text.toString().trim().ifEmpty { "Time to drink water! 💧" }
            val minStr = etMinutes.text.toString().trim()
            val min = minStr.toDoubleOrNull() ?: 10.0

            val reminder = AlarmScheduler.scheduleAlarm(this, sender, msg, min)
            Toast.makeText(this, "Reminder set for ${reminder.sender} in $min mins!", Toast.LENGTH_SHORT).show()
            renderReminders()
        }

        btnTestDrop.setOnClickListener {
            val sender = etSender.text.toString().trim()
            val msg = etMessage.text.toString().trim().ifEmpty { "Time to drink water! 💧" }
            triggerImmediateDrop(sender, msg)
        }
    }

    override fun onResume() {
        super.onResume()
        renderReminders()
        handler.post(tickRunnable)
    }

    override fun onPause() {
        handler.removeCallbacks(tickRunnable)
        super.onPause()
    }

    private fun renderReminders() {
        containerActiveReminders.removeAllViews()
        val reminders = ReminderStore.getAll(this).sortedBy { it.triggerTimeMs }

        if (reminders.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No active reminders scheduled."
                setTextColor(0xFF64748B.toInt())
                textSize = 13f
                setPadding(0, 12, 0, 12)
            }
            containerActiveReminders.addView(emptyTv)
            return
        }

        val now = System.currentTimeMillis()
        reminders.forEach { r ->
            val remainingMins = ((r.triggerTimeMs - now) / 60000.0).coerceAtLeast(0.0)
            val timeFormatted = DateFormat.getTimeFormat(this).format(Date(r.triggerTimeMs))

            val itemView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(0xFF161B26.toInt())
                setPadding(16, 14, 16, 14)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 12)
                layoutParams = params
            }

            val textLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            }

            val tvTitle = TextView(this).apply {
                text = "👤 From: ${r.sender}"
                setTextColor(0xFF00D2FF.toInt())
                textSize = 14f
            }

            val tvMsg = TextView(this).apply {
                text = "💬 ${r.message}"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 14f
            }

            val tvTime = TextView(this).apply {
                text = "⏰ At: $timeFormatted (in ${String.format("%.1f", remainingMins)} mins)"
                setTextColor(0xFF94A3B8.toInt())
                textSize = 12f
            }

            textLayout.addView(tvTitle)
            textLayout.addView(tvMsg)
            textLayout.addView(tvTime)

            val btnCancel = Button(this).apply {
                text = "CANCEL"
                setBackgroundColor(0xFFE62429.toInt())
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 11f
                setOnClickListener {
                    AlarmScheduler.cancelAlarm(this@MainActivity, r.id)
                    renderReminders()
                    Toast.makeText(this@MainActivity, "Reminder cancelled!", Toast.LENGTH_SHORT).show()
                }
            }

            itemView.addView(textLayout)
            itemView.addView(btnCancel)

            containerActiveReminders.addView(itemView)
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please allow 'Display over other apps' permission", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun triggerImmediateDrop(sender: String, message: String) {
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("EXTRA_SENDER", if (sender.isBlank()) "Spider-Man 🕷️" else sender)
            putExtra("EXTRA_REMINDER_TEXT", message)
            putExtra("EXTRA_TRIGGER_TIME", System.currentTimeMillis())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
