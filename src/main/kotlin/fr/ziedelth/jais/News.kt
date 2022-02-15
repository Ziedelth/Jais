/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import org.jsoup.Jsoup
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import kotlin.math.min

fun main(args: Array<String>) {
    // https://www.crunchyroll.com/newsrss?lang=frFR
    // https://www.animenewsnetwork.com/all/rss.xml?ann-edition=fr
    // https://www.wakanim.tv/fr/v2/news

    val calendar = Calendar.getInstance()
    val gson = GsonBuilder().setPrettyPrinting().create()
    val xmlMapper = XmlMapper()
    val objectMapper = ObjectMapper()

    val v = "Nouveau visuel spécial ! Pour célébrer la journée de l'amour, l'anime The Quintessential Quintuplets, a publié un visuel des cinq sœurs Nakano vous souhaitant une \"joyeuse Saint-Valentin\"."
    println(v.substring(0 until min(v.length, 100)))

    println("-- Anime Digital Network")
    adn(gson, objectMapper, xmlMapper, calendar)
    println("-- Crunchyroll")
    crunchyroll(gson, objectMapper, xmlMapper, calendar)
}

private fun adn(
    gson: Gson,
    objectMapper: ObjectMapper,
    xmlMapper: XmlMapper,
    calendar: Calendar
) {
    val inputStream = URL("https://www.animenewsnetwork.com/all/rss.xml?ann-edition=fr").openStream()
    val jsonObject: JsonObject? = gson.fromJson(
        objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
        JsonObject::class.java
    )
    inputStream.close()

    Impl.getArray(Impl.getObject(jsonObject, "channel"), "item")?.forEach {
        val category = Impl.getString(it.asJsonObject, "category")
        if (!(category.equals("Anime", true) || category.equals("Manga", true))) return@forEach
        val title = Impl.getString(it.asJsonObject, "title")
        val description = Jsoup.parse(Impl.getString(it.asJsonObject, "description") ?: "").text()
        val url = Impl.getString(it.asJsonObject, "link")
        val releaseDate = ISO8601.fromUTCDate(ISO8601.fromCalendar2(Impl.getString(it.asJsonObject, "pubDate"))) ?: return@forEach

        if (!ISO8601.isSameDayUsingInstant(calendar, releaseDate) || calendar.before(releaseDate)) return@forEach

        println(title)
        println(description)
        println(url)
        println(ISO8601.fromUTCDate(releaseDate))
        println()
    }
}

private fun crunchyroll(
    gson: Gson,
    objectMapper: ObjectMapper,
    xmlMapper: XmlMapper,
    calendar: Calendar
) {
    val inputStream = URL("https://www.crunchyroll.com/newsrss?lang=frFR").openStream()
    val jsonObject: JsonObject? = gson.fromJson(
        objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
        JsonObject::class.java
    )
    inputStream.close()

    Impl.getArray(Impl.getObject(jsonObject, "channel"), "item")?.forEach {
        val title = Impl.getString(it.asJsonObject, "title")
        val description = Jsoup.parse(Impl.getString(it.asJsonObject, "description") ?: "").text()
        val url = Impl.getString(it.asJsonObject, "guid")
        val releaseDate = ISO8601.fromUTCDate(ISO8601.fromCalendar2(Impl.getString(it.asJsonObject, "pubDate"))) ?: return@forEach

        if (!ISO8601.isSameDayUsingInstant(calendar, releaseDate) || calendar.before(releaseDate)) return@forEach

        println(title)
        println(description)
        println(url)
        println(ISO8601.fromUTCDate(releaseDate))
        println()
    }
}