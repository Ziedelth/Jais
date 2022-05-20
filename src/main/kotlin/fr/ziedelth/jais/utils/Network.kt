package fr.ziedelth.jais.utils

import java.io.InputStreamReader
import java.net.URL

object Network {
    data class NetworkResponse(val isSuccess: Boolean, val content: String)

    fun connect(uri: String, timeout: Int = 5000): NetworkResponse {
        return try {
            val urlConnection = URL(uri).openConnection().apply {
                connectTimeout = timeout
                readTimeout = timeout
            }

            val inputStream = InputStreamReader(urlConnection.inputStream)
            val content = inputStream.readLines().joinToString("\n")
            inputStream.close()

            NetworkResponse(true, content)
        } catch (e: Exception) {
            NetworkResponse(false, e.message ?: "")
        }
    }
}