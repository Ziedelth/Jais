/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.net.InetSocketAddress
import java.net.Socket
import java.util.logging.Level

/* Kotlin is a language that is statically typed. This means that the compiler will check that the types of the variables
are correct. */
object Impl {
    /* This is a function extension. It's a function that is added to the String class. */
    fun String.toHTTPS() = this.replace("http://", "https://")

    /**
     * If the given jsonElement is a JsonObject, return it. Otherwise, return null
     *
     * @param jsonElement The JsonElement to be converted to a JsonObject.
     */
    fun toObject(jsonElement: JsonElement?): JsonObject? =
        if (jsonElement?.isJsonObject == true) jsonElement.asJsonObject else null

    fun getObject(jsonObject: JsonObject?, key: String): JsonObject? =
        if (jsonObject?.has(key) == true && jsonObject[key].isJsonObject) jsonObject[key].asJsonObject else null

    /**
     * If the JSON element is a string, return it. Otherwise, return null
     *
     * @param jsonElement The JsonElement to be converted to a String.
     */
    fun toString(jsonElement: JsonElement?): String? =
        if (jsonElement?.isJsonPrimitive == true && jsonElement.asJsonPrimitive.isString) jsonElement.asString else null

    /**
     * If the JSON object has the key and the value is a string, return the string
     *
     * @param jsonObject The JsonObject to search for the key.
     * @param key The key to look for in the JSON object.
     */
    fun getString(jsonObject: JsonObject?, key: String): String? =
        if (jsonObject?.has(key) == true && jsonObject[key].isJsonPrimitive && jsonObject[key].asJsonPrimitive.isString) jsonObject[key].asString else null

    /**
     * If the JSON object has the key and the value is a JSON array, return the value as a JSON array
     *
     * @param jsonObject The JsonObject to search for the key.
     * @param key The key to look for in the JsonObject.
     */
    fun getArray(jsonObject: JsonObject?, key: String): JsonArray? =
        if (jsonObject?.has(key) == true && jsonObject[key].isJsonArray) jsonObject[key].asJsonArray else null

    /**
     * If the JSON object has the key and the value is a number, return the number as a long
     *
     * @param jsonObject The JsonObject to search for the key.
     * @param key The key to look for in the JSON object.
     */
    fun getLong(jsonObject: JsonObject?, key: String): Long? =
        if (jsonObject?.has(key) == true && jsonObject[key].isJsonPrimitive && jsonObject[key].asJsonPrimitive.isNumber) jsonObject[key].asLong else null

    /**
     * If an exception is thrown, the function will log the exception with the given error message
     *
     * @param errorMessage The error message to be displayed if the action fails.
     * @param action The action to be performed.
     */
    fun tryCatch(errorMessage: String? = null, action: () -> Unit) {
        try {
            action.invoke()
        } catch (exception: Exception) {
            if (!errorMessage.isNullOrEmpty()) JLogger.log(Level.WARNING, errorMessage, exception)
        }
    }

    /**
     * If the action throws an exception, run the errorAction
     *
     * @param action The action to be performed.
     * @param errorAction () -> Unit
     */
    fun tryCatch(action: () -> Unit, errorAction: () -> Unit) {
        try {
            action.invoke()
        } catch (exception: Exception) {
            JLogger.log(Level.WARNING, "Error on try / catch", exception)
            errorAction.invoke()
        }
    }

    /**
     * "Check if there's an internet connection by trying to connect to google.com on port 80."
     *
     * The function has a single parameter, which is a lambda. The lambda is the last parameter of the function, and it's
     * the only one that is annotated with the `@Throws` annotation
     *
     * @return Nothing.
     */
    fun hasInternet(): Boolean {
        var socket: Socket? = null

        return try {
            socket = Socket()
            val socketAddress = InetSocketAddress("google.com", 80)
            socket.connect(socketAddress, 10000)
            true
        } catch (exception: Exception) {
            false
        } finally {
            socket?.close()
        }
    }
}