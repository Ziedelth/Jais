package fr.ziedelth.jais

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request

fun main() {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://unogs-unogs-v1.p.rapidapi.com/title/episodes?season_id=1&netflix_id=81228573")
        .get()
        .addHeader("X-RapidAPI-Host", "unogs-unogs-v1.p.rapidapi.com")
        .addHeader("X-RapidAPI-Key", "cedd44ca52mshd9502590704785cp12abb6jsn2019b9852486")
        .build()

    val response = client.newCall(request).execute()
    println(response.body()?.string())
}