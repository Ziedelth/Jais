/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais

import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

fun main(args: Array<String>) {
    val siteUrl = "https://cubari.moe"
    val urls = arrayOf("/read/gist/GoblinSlayer/", "/read/gist/GSSSYO/")

    urls.forEach { url ->
        println("$siteUrl$url")

        val page = Jsoup.connect("$siteUrl$url").get()
        val contentElement = page.getElementsByTag("content").first()

        val anime = contentElement?.getElementsByTag("h1")?.first()?.text()
        println(anime)
        val animeImage = page.getElementsByTag("picture").first()?.getElementsByTag("img")?.first()?.attr("src")
        println(animeImage)
        val animeDescription = contentElement?.getElementsByTag("p")?.first()?.text()
        println(animeDescription)

        page.getElementsByClass("table-default").forEach { element ->
            val number = element.attr("data-chapter")
            val scanUrl = "$siteUrl${element.getElementsByTag("a").first()?.attr("href")}"
            val literalScanDate = element.getElementsByClass("detailed-chapter-upload-date").first()?.text()?.replace("[", "")?.replace("]", "")?.split(", ")
            // Take the 3 first elements
            val scanDate = literalScanDate?.take(3)
            // Convert scanDate to a Date
            val date = Calendar.getInstance()
            date.set(scanDate?.get(0)?.toInt()!!, scanDate[1].toInt(), scanDate[2].toInt())
            // Create object SimpleDateFormat "dd/MM/yyyy" and format date
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy").format(date.time)
            // Print the chapter number and the url. Separated by a tab
            println("$number\t$scanUrl\t$dateFormatter")
        }

        println()
    }
}