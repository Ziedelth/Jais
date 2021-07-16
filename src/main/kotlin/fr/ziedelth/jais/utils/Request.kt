package fr.ziedelth.jais.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object Request {
    private fun getResponse(connection: HttpURLConnection): String {
        val reader = BufferedReader(InputStreamReader(connection.inputStream, Const.DEFAULT_CHARSET))
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

}