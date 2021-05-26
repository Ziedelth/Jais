package fr.ziedelth.ziedbot.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.ziedelth.ziedbot.commands.ClearCommand
import fr.ziedelth.ziedbot.platforms.AnimeDigitalNetwork
import fr.ziedelth.ziedbot.platforms.Crunchyroll
import fr.ziedelth.ziedbot.platforms.Wakanim
import fr.ziedelth.ziedbot.utils.animes.Platform
import fr.ziedelth.ziedbot.utils.commands.Command
import fr.ziedelth.ziedbot.utils.tokens.DiscordToken
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.experimental.and

object Const {
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    val platforms: Array<Platform> = arrayOf(Crunchyroll(), Wakanim(), AnimeDigitalNetwork())
    val commands: Array<Command> = arrayOf(ClearCommand())
    val discordToken: DiscordToken = GSON.fromJson(
        Files.readString(File("tokens", "discord.json").toPath(), StandardCharsets.UTF_8),
        DiscordToken::class.java
    )

    fun encode(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(bytes)
        val sb = StringBuilder()
        for (b in digest) sb.append(((b and 0xff.toByte()) + 0x100).toString(16).substring(1))
        return sb.toString()
    }
}