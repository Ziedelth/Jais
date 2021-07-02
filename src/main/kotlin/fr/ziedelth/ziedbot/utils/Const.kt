package fr.ziedelth.ziedbot.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.ziedelth.ziedbot.platforms.AnimeDigitalNetwork
import fr.ziedelth.ziedbot.platforms.Crunchyroll
import fr.ziedelth.ziedbot.platforms.Wakanim
import fr.ziedelth.ziedbot.utils.animes.Platform
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and

object Const {
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    val CLIENTS: MutableList<Client> = mutableListOf()
    val PLATFORMS: Array<Platform> = arrayOf(AnimeDigitalNetwork(), Crunchyroll(), Wakanim())
    const val DELAY_BETWEEN_REQUEST = 3L
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

    fun encode(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(bytes)
        val sb = StringBuilder()
        for (b in digest) sb.append(((b and 0xff.toByte()) + 0x100).toString(16).substring(1))
        return sb.toString()
    }

    fun generate(length: Int): String {
        val leftLimit = 48 // numeral '0'
        val rightLimit = 122 // letter 'z'
        val random = Random()
        return random.ints(leftLimit, rightLimit + 1).filter { i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97) }
            .limit(length.toLong())
            .collect({ StringBuilder() }, java.lang.StringBuilder::appendCodePoint, java.lang.StringBuilder::append)
            .toString()
    }
}