package com.spydy.reminder

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object ReminderStore {
    private const val PREFS_NAME = "spydy_prefs"
    private const val KEY_REMINDERS = "reminders_json"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun save(context: Context, reminder: Reminder) {
        val reminders = getAll(context).toMutableList()
        reminders.removeAll { it.id == reminder.id }
        reminders.add(reminder)
        saveAll(context, reminders)
    }

    fun remove(context: Context, id: String) {
        val reminders = getAll(context).filter { it.id != id }
        saveAll(context, reminders)
    }

    fun getAll(context: Context): List<Reminder> {
        val jsonStr = getPrefs(context).getString(KEY_REMINDERS, "[]") ?: "[]"
        val list = mutableListOf<Reminder>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Reminder(
                        id = obj.getString("id"),
                        sender = obj.optString("sender", "Spider-Man"),
                        message = obj.optString("message", "Time to drink water! 💧"),
                        triggerTimeMs = obj.optLong("triggerTimeMs", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveAll(context: Context, list: List<Reminder>) {
        val array = JSONArray()
        list.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("sender", r.sender)
                put("message", r.message)
                put("triggerTimeMs", r.triggerTimeMs)
            }
            array.put(obj)
        }
        getPrefs(context).edit().putString(KEY_REMINDERS, array.toString()).apply()
    }
}
