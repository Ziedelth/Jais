package fr.ziedelth.ziedbot.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.ziedelth.ziedbot.platforms.AnimeDigitalNetwork
import fr.ziedelth.ziedbot.platforms.Crunchyroll
import fr.ziedelth.ziedbot.platforms.Wakanim
import fr.ziedelth.ziedbot.utils.animes.Platform
import java.io.File
import java.security.MessageDigest
import kotlin.experimental.and

object Const {
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    val CLIENTS: MutableList<Client> = mutableListOf()
    val PLATFORMS: Array<Platform> = arrayOf(AnimeDigitalNetwork(), Crunchyroll(), Wakanim())
    val GUILDS_FOLDER = File("guilds")
        get() {
            if (!field.exists()) field.mkdirs()
            return field
        }

    const val DELAY_BETWEEN_REQUEST = 1L
    const val SEND_MESSAGES = true

    fun encode(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(bytes)
        val sb = StringBuilder()
        for (b in digest) sb.append(((b and 0xff.toByte()) + 0x100).toString(16).substring(1))
        return sb.toString()
    }
}