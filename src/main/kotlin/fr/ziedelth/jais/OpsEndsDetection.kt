/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais

import com.google.gson.Gson
import fr.ziedelth.jais.utils.JBrowser
import fr.ziedelth.jais.utils.animes.sql.JMapper
import fr.ziedelth.jais.utils.animes.sql.data.AnimeData
import fr.ziedelth.jais.utils.animes.sql.data.OpsEndsData
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.util.*

fun containsId(list: List<OpsEndsData>, id: Long) = list.any { it.animeId == id }

fun main() {
    val connection = JMapper.getDebugConnection()
    val animes = JMapper.animeMapper.get(connection)
    val opsends = JMapper.opsEndsMapper.get(connection)

    val gson = Gson()
    val file = File("test.json")

    if (!file.exists()) {
        file.createNewFile()
        Files.writeString(file.toPath(), gson.toJson(emptyArray<Long>()))
    }

    val array = gson.fromJson(FileReader(file), Array<Long>::class.java).toMutableList()

    animes.filter { it.episodes.isNotEmpty() && !containsId(opsends, it.id) }.sortedWith(compareBy(AnimeData::name))
        .forEach { anime ->
            if (array.contains(anime.id)) return@forEach

            println("Search anime ${anime.name} (${anime.id})...")
            val document = JBrowser.get(
                "https://www.animesonglyrics.com/results?q=${
                    anime.name.replace(' ', '+').replace("'", "%27")
                }"
            )
            val elements =
                document?.selectXpath("//*[@id=\"titlelist\"]")?.firstOrNull()?.getElementsByClass("homesongs")

            if (elements.isNullOrEmpty()) {
                println("No anime found, skipping...")
                array.add(anime.id)
                Files.writeString(file.toPath(), gson.toJson(array))
                return@forEach
            }

            println("${animes.indexOf(anime) + 1}/${animes.size} -> ${anime.name}")
            println("------------------------------------------------------------")

            elements.forEachIndexed { index, it ->
                val element = it.getElementsByTag("a").firstOrNull()
                val url = element?.attr("href")
                val title = element?.attr("data-original-title")
                println("[$index] $url -- $title")
            }

            println()
            print("Please select index >> ")
            val input = readLine()

            if (input.isNullOrEmpty() || input == "-1") {
                array.add(anime.id)
                Files.writeString(file.toPath(), gson.toJson(array))
                return@forEach
            }

            input.split(",").forEachIndexed { _, s ->
                if (s.toIntOrNull() == null)
                    return@forEachIndexed

                val element = elements[s.toInt()]?.getElementsByTag("a")?.firstOrNull()
                val url = element?.attr("href")

                val animeDocument = JBrowser.get(url)

                val songs = animeDocument?.getElementsByTag("a")?.filter { links ->
                    val t = links?.attr("data-original-title")

                    return@filter links.hasClass("list-group-item") && (t?.contains("Opening", true) == true || t?.contains("Ending", true) == true)
                }

                if (songs.isNullOrEmpty()) {
                    println("No anime songs found, skipping...")
                    return@forEachIndexed
                }

                println("Total songs: ${songs.size}")

                songs.forEachIndexed { songIndex, song ->
                    println("Song: ${songIndex + 1}/${songs.size}")

                    val songUrl = song?.attr("href")
                    val dot = song?.attr("data-original-title")
                    val songType =
                        if (dot?.contains("Opening", true) == true) 1L
                        else if (dot?.contains("Ending", true) == true) 2L
                        else 0L
                    val songName = song?.text()

                    val youtubeSongUrl = JBrowser.get(songUrl)?.selectXpath("//*[@id=\"plysng\"]/a")?.attr("href")

                    println("Inserting url")

                    if (songType != 0L && !youtubeSongUrl.isNullOrEmpty()) {
                        JMapper.opsEndsMapper.insert(connection, anime.id, songType, songName, youtubeSongUrl)
                        println("Inserted!")
                    } else
                        println("Can not insert ops ends")
                }
            }
        }
}