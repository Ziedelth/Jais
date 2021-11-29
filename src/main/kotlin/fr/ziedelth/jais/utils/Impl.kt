/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import fr.ziedelth.jais.utils.debug.JLogger
import java.util.logging.Level

object Impl {
    fun String.toHTTPS() = this.replace("http://", "https://")

    fun toObject(jsonElement: JsonElement?): JsonObject? =
        if (jsonElement?.isJsonObject == true) jsonElement.asJsonObject else null

    fun getObject(jsonObject: JsonObject?, key: String): JsonObject? =
        if (jsonObject?.has(key) == true && jsonObject[key].isJsonObject) jsonObject[key].asJsonObject else null

    fun toString(jsonElement: JsonElement?): String? =
        if (jsonElement?.isJsonPrimitive == true && jsonElement.asJsonPrimitive.isString) jsonElement.asString else null

    fun getString(jsonObject: JsonObject?, key: String): String? =
        if (jsonObject?.has(key) == true && jsonObject[key].isJsonPrimitive && jsonObject[key].asJsonPrimitive.isString) jsonObject[key].asString else null

    fun getArray(jsonObject: JsonObject?, key: String): JsonArray? =
        if (jsonObject?.has(key) == true && jsonObject[key].isJsonArray) jsonObject[key].asJsonArray else null

    fun getLong(jsonObject: JsonObject?, key: String): Long? =
        if (jsonObject?.has(key) == true && jsonObject[key].isJsonPrimitive && jsonObject[key].asJsonPrimitive.isNumber) jsonObject[key].asLong else null

    fun tryCatch(errorMessage: String? = null, action: () -> Unit) {
        try {
            action.invoke()
        } catch (exception: Exception) {
            if (!errorMessage.isNullOrEmpty()) JLogger.log(Level.WARNING, errorMessage, exception)
        }
    }
}