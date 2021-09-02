/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.ziedelth.jais.platforms.AnimeDigitalNetwork
import fr.ziedelth.jais.platforms.Crunchyroll
import fr.ziedelth.jais.platforms.MangaScan
import fr.ziedelth.jais.platforms.Wakanim
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Platform
import fr.ziedelth.jais.utils.clients.Client
import fr.ziedelth.jais.utils.tokens.Token
import org.w3c.dom.NodeList
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URLConnection
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.floor
import kotlin.math.min

object Const {
    private val fmt: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    const val DELAY_BETWEEN_REQUEST = 1L
    const val SEND_MESSAGES = true
    const val PUBLIC = true
    const val DISPLAY = 5

    val CLIENTS: MutableList<Client> = mutableListOf()
    val PLATFORMS: Array<Platform> = arrayOf(AnimeDigitalNetwork(), Crunchyroll(), MangaScan(), Wakanim())
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    val DEFAULT_CHARSET: Charset = StandardCharsets.UTF_8
    val MAIN_COLOR: Color = Color.YELLOW

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
    fun isSameDay(var0: Calendar, var1: Calendar): Boolean = this.fmt.format(var0.time) == this.fmt.format(var1.time)

    fun String.toHHMMSS(): String {
        val secNum = this.toInt(10)
        val hours = floor(secNum / 3600.0)
        val minutes = floor((secNum - (hours * 3600.0)) / 60.0)
        val seconds = secNum - (hours * 3600.0) - (minutes * 60.0)
        var h: String = hours.toInt().toString()
        var m: String = minutes.toInt().toString()
        var s: String = seconds.toInt().toString()

        if (hours < 10) h = "0${hours.toInt()}"
        if (minutes < 10) m = "0${minutes.toInt()}"
        if (seconds < 10) s = "0${seconds.toInt()}"

        return "${(if (hours >= 1) "$h:" else "")}$m:$s"
    }

    fun Long.toHHMMSS(): String = this.toString().toHHMMSS()

    fun getEpisodeMessage(episode: Episode): String =
        "🔜 ${episode.anime}\n${if (episode.title != null) "${episode.title}\n" else ""}" +
                "${episode.country.season} ${episode.season} • ${episode.country.episode} ${episode.number} ${if (episode.type == EpisodeType.SUBTITLED) episode.country.subtitled else episode.country.dubbed}\n" +
                "${Emoji.CLAP} ${episode.duration.toHHMMSS()}\n" +
                "#Anime #${episode.platform.getName().replace(" ", "")}\n" +
                "\n" +
                "${episode.url}"

    fun getItems(url: URLConnection, key: String): NodeList {
        val dbf = DocumentBuilderFactory.newInstance()
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(url.getInputStream())
        doc.documentElement.normalize()
        return doc.getElementsByTagName(key)
    }

    fun generateToken(length: Long): String = (1..length)
        .map { kotlin.random.Random.nextInt(0, this.charPool.size) }
        .map(this.charPool::get)
        .joinToString("")

    fun getToken(string: String, token: Class<out Token>): Token? {
        val tokenFile = File(TOKENS_FOLDER, string)

        if (!tokenFile.exists()) {
            JLogger.warning("Token file $string not exists!")
            Files.writeString(tokenFile.toPath(), GSON.toJson(token), DEFAULT_CHARSET)
            return null
        }

        val tToken = GSON.fromJson(
            Files.readString(
                tokenFile.toPath(),
                DEFAULT_CHARSET
            ), token
        )

        if (tToken == null || tToken.isEmpty()) {
            JLogger.warning("Token file $string is empty!")
            return null
        }

        return tToken
    }

    fun getAnimes(episodes: Array<Episode>): Array<String> = episodes.map { it.anime }.distinct().toTypedArray()
    fun getImages(animes: Array<String>, episodes: Array<Episode>): Array<ByteArrayInputStream> =
        animes.mapNotNull { anime -> episodes.firstOrNull { episode -> episode.anime.equals(anime, true) } }
            .mapNotNull { it.downloadedImage }.toTypedArray()

    fun getAnimesMessage(animes: Array<String>): String {
        val stringBuilder = StringBuilder()
        animes.forEach { stringBuilder.append("• $it\n") }
        return stringBuilder.toString()
    }
}