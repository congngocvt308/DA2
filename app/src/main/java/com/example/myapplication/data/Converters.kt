package com.example.myapplication.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return value?.let { gson.fromJson(it, listType) } ?: emptyList()
    }

    @TypeConverter
    fun listToString(list: List<String>?): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromStringSet(value: String?): Set<String> {
        val setType = object : TypeToken<Set<String>>() {}.type
        return value?.let { gson.fromJson(it, setType) } ?: emptySet()
    }

    @TypeConverter
    fun setToString(set: Set<String>?): String {
        return gson.toJson(set)
    }
}