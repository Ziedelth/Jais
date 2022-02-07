/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais

import fr.ziedelth.jais.utils.JBrowser
import java.text.SimpleDateFormat
import java.util.*

fun main(args: Array<String>) {
    val calendar = GregorianCalendar.getInstance()
    val f = SimpleDateFormat("dd/MM/yyyy").format(calendar.time)

    val year = calendar.get(Calendar.YEAR)
    val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)

    val base = "https://www.nautiljon.com"
    val elements = JBrowser.get("$base/planning/manga/?y=$year&m=$month")?.getElementsByTag("tr")

    elements?.removeFirst()
    elements?.removeFirst()
    elements?.removeLast()

    elements?.filter { it.getElementsByTag("td").firstOrNull()?.text()?.equals(f, true) == true }?.forEach {
        val tags = it.getElementsByTag("td")
        val date = tags.firstOrNull()?.text()
        println("Date: $date")
        val image = tags[1]?.getElementsByTag("img")?.firstOrNull()?.attr("src")
        println("Image: $base$image")
        val title = tags[2]?.text()
        println("Title: $title")
        val price = tags[3]?.text()
        println("Price: $price")
        val editor = tags[4]?.text()
        println("Editor: $editor")
        val link = tags[5]?.getElementsByTag("a")?.firstOrNull()?.attr("href")
        if (!link.isNullOrBlank())
            println("Link: $base$link")
        println()
    }
}