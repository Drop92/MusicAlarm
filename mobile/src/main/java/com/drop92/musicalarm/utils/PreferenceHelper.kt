package com.drop92.musicalarm.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object PreferenceHelper {

    fun defPreference(ctx: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx)

    fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    fun SharedPreferences.setValue(key: String, value: Any){
        when(value) {
            is String -> edit({it.putString(key, value)})
            is Boolean -> edit{it.putBoolean(key, value)}
            is Long -> edit({it.putLong(key, value)})
            is Int -> edit({it.putInt(key, value)})
        }
    }

    fun <T> SharedPreferences.getValue(key:String, defaultValue: T): T {
        return when (defaultValue) {
            is String  -> getString(key, defaultValue) as T
            is Boolean -> getBoolean(key, defaultValue) as T
            is Long -> getLong(key,defaultValue) as T
            is Int -> getInt(key,defaultValue) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }
}

