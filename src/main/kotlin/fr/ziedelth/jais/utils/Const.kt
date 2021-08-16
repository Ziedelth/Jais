/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import com.google.common.hash.Hashing
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.ziedelth.jais.platforms.AnimeDigitalNetwork
import fr.ziedelth.jais.platforms.Crunchyroll
import fr.ziedelth.jais.platforms.MangaScan
import fr.ziedelth.jais.platforms.Wakanim
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeBuilder
import fr.ziedelth.jais.utils.animes.Platform
import fr.ziedelth.jais.utils.clients.Client
import java.awt.Color
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

object Const {
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    val CLIENTS: MutableList<Client> = mutableListOf()
    val PLATFORMS: Array<Platform> = arrayOf(AnimeDigitalNetwork(), Crunchyroll(), MangaScan(), Wakanim())
    const val DELAY_BETWEEN_REQUEST = 2L
    const val SEND_MESSAGES = true
    val DEFAULT_CHARSET: Charset = StandardCharsets.UTF_8
    val GUILDS_FOLDER = File("guilds")
        get() {
            if (!field.exists()) field.mkdirs()
            return field
        }
    val TOKENS_FOLDER = File("tokens")
        get() {
            if (!field.exists()) field.mkdirs()
            return field
        }
    val MAIN_COLOR: Color = Color.YELLOW
    const val PUBLIC: Boolean = true
    const val DISPLAY = 5

    private fun encodeSHA512(string: String) = Hashing.sha512().hashString(string, DEFAULT_CHARSET).toString()
    fun encodeMD5(string: String) = Hashing.md5().hashString(string, DEFAULT_CHARSET).toString()

    fun toInt(string: String): String = try {
        "${string.toInt()}"
    } catch (exception: Exception) {
        string
    }

    fun toInt(string: String?, stringError: String, prefix: String? = null): String = try {
        "${if (prefix.isNullOrEmpty()) "" else prefix} ${string?.toInt()}"
    } catch (exception: Exception) {
        stringError
    }

    fun substring(string: String, int: Int) = string.substring(0, min(string.length, int))
    fun toHexString(color: Color): String = String.format("#%06X", 0xFFFFFF and color.rgb)
    fun toId(episode: Episode): String =
        encodeSHA512("${episode.platform.getName()}${episode.anime}${episode.season}${episode.number}${episode.country}${episode.type}")

    fun toId(episodeBuilder: EpisodeBuilder): String =
        encodeSHA512("${episodeBuilder.platform.getName()}${episodeBuilder.anime}${episodeBuilder.number}${episodeBuilder.country}${episodeBuilder.type}")

    fun isSameDay(var0: Calendar, var1: Calendar): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd")
        return fmt.format(var0.time) == fmt.format(var1.time)
    }
}