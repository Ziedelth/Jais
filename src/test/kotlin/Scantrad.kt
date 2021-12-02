/*
 * Copyright (c) 2021. Ziedelth
 */

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.ziedelth.jais.utils.ISO8601
import org.jsoup.Jsoup
import java.io.InputStreamReader
import java.net.URL

/*
 * Copyright (c) 2021. Ziedelth
 */

fun main() {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val xmlMapper = XmlMapper()
    val objectMapper = ObjectMapper()
    val inputStream = URL("https://scantrad.net/rss/").openStream()
    val jsonObject: JsonObject? = gson.fromJson(
        objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
        JsonObject::class.java
    )

    jsonObject?.get("channel")?.asJsonObject?.get("item")?.asJsonArray?.forEach {
        val scanObject = it.asJsonObject
        val titleSplitter = scanObject.get("title")?.asString?.split("Scan - ")?.get(1)?.split(" ")
        val descriptionObject = scanObject.get("description")?.asString
        val descriptionDocument = Jsoup.parse(descriptionObject ?: "")
        val animeLink = descriptionDocument.getElementsByTag("a").attr("href")

        val releaseDate = ISO8601.toCalendar2(scanObject.get("pubDate")?.asString)
        val title = titleSplitter?.subList(0, titleSplitter.size - 2)?.joinToString(" ")
        val number = titleSplitter?.lastOrNull()?.toLongOrNull()
        val link = scanObject.get("link")?.asString
        val animeImage = descriptionDocument.getElementsByTag("img").attr("src")

        println("$title [$number] (// ${ISO8601.fromUTCDate(releaseDate)} \\\\) : $animeLink $link \\\\// $animeImage")
    }
}