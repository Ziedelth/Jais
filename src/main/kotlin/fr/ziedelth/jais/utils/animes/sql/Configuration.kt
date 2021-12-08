/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql

import com.google.gson.Gson
import java.io.File
import java.io.FileReader

data class Configuration(val url: String, val user: String, val password: String) {
    companion object {
        fun load(): Configuration? {
            val file = File("database.json")
            return if (!file.exists()) null else Gson().fromJson(FileReader(file), Configuration::class.java)
        }
    }
}
