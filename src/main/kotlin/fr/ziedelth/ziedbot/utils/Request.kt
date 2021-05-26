package fr.ziedelth.ziedbot.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object Request {
    fun isOnline(url: String): Boolean {
        val connection: HttpURLConnection = connection(url)
        val responseCode: Int = connection.responseCode
        return (responseCode in 200..399)
    }

    private fun getResponse(connection: HttpURLConnection): String {
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val stringBuilder = StringBuilder()
        reader.forEachLine { stringBuilder.append(it) }
        reader.close()
        connection.disconnect()
        return stringBuilder.toString()
    }

    private fun connection(url: String): HttpURLConnection {
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.doOutput = true
        connection.instanceFollowRedirects = false
        connection.useCaches = false
        connection.setRequestProperty("charset", "utf-8")
        return connection
    }

    fun get(url: String): String {
        val connection: HttpURLConnection = connection(url)
        connection.requestMethod = "GET"
        return getResponse(connection)
    }

    fun post(url: String, post: String): String {
        val connection: HttpURLConnection = connection(url)
        connection.requestMethod = "POST"

        val dataOutputStream = DataOutputStream(connection.outputStream)
        dataOutputStream.write(post.toByteArray(StandardCharsets.UTF_8))
        dataOutputStream.flush()
        dataOutputStream.close()

        return getResponse(connection)
    }
}