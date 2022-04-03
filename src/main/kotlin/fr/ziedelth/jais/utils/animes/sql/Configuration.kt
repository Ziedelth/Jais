/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql

import com.google.gson.Gson
import fr.ziedelth.jais.utils.FileImpl
import java.io.FileReader

data class Configuration(val url: String, val user: String, val password: String) {
    companion object {
        /**
         * Loads the database.json file and returns a Configuration object if it exists, otherwise returns null
         *
         * @return The configuration object.
         */
        fun load(): Configuration? {
            val file = FileImpl.getFile("database.json")
            return if (!file.exists()) null else Gson().fromJson(FileReader(file), Configuration::class.java)
        }
    }
}
