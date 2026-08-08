package com.spydy.reminder

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etMessage: EditText
    private lateinit var etMinutes: EditText
    private lateinit var btnSetCustom: Button
    private lateinit var btnTestDrop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkOverlayPermission()

        etMessage = findViewById(R.id.etMessage)
        etMinutes = findViewById(R.id.etMinutes)
        btnSetCustom = findViewById(R.id.btnSetCustom)
        btnTestDrop = findViewById(R.id.btnTestDrop)

        btnSetCustom.setOnClickListener {
            val msg = etMessage.text.toString().ifEmpty { "Time to drink water! 💧" }
            val minStr = etMinutes.text.toString()
            val min = minStr.toDoubleOrNull() ?: 10.0

            AlarmScheduler.scheduleAlarm(this, msg, min)
            Toast.makeText(this, "Reminder scheduled in $min minutes!", Toast.LENGTH_SHORT).show()
        }

        btnTestDrop.setOnClickListener {
            val msg = etMessage.text.toString().ifEmpty { "Time to drink water! 💧" }
            triggerImmediateDrop(msg)
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please allow 'Display over other apps' permission", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun triggerImmediateDrop(message: String) {
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("EXTRA_REMINDER_TEXT", message)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
