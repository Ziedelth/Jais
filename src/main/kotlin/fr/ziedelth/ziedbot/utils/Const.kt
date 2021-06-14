package fr.ziedelth.ziedbot.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.ziedelth.ziedbot.ZiedBot
import fr.ziedelth.ziedbot.commands.AnimeCommand
import fr.ziedelth.ziedbot.commands.ClearCommand
import fr.ziedelth.ziedbot.platforms.AnimeDigitalNetwork
import fr.ziedelth.ziedbot.platforms.Crunchyroll
import fr.ziedelth.ziedbot.platforms.Wakanim
import fr.ziedelth.ziedbot.utils.animes.Platform
import fr.ziedelth.ziedbot.utils.commands.Command
import fr.ziedelth.ziedbot.utils.tokens.DiscordToken
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import java.time.temporal.TemporalAccessor
import kotlin.experimental.and

object Const {
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    val PLATFORMS: Array<Platform> = arrayOf(Crunchyroll(), AnimeDigitalNetwork(), Wakanim())
    val COMMANDS: Array<Command> = arrayOf(ClearCommand(), AnimeCommand())
    val DISCORD_TOKEN: DiscordToken = GSON.fromJson(
        Files.readString(File("tokens", "discord.json").toPath(), StandardCharsets.UTF_8),
        DiscordToken::class.java
    )
    val GUILDS_FOLDER = File("guilds")
        get() {
            if (!field.exists()) field.mkdirs()
            return field
        }
    val DELAY_BETWEEN_REQUEST = 3L

    fun encode(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(bytes)
        val sb = StringBuilder()
        for (b in digest) sb.append(((b and 0xff.toByte()) + 0x100).toString(16).substring(1))
        return sb.toString()
    }

    fun setEmbed(
        author: String? = null,
        authorUrl: String? = null,
        authorIcon: String? = null,
        title: String? = null,
        titleUrl: String? = null,
        thumbnail: String? = null,
        description: String? = null,
        color: Color? = null,
        image: String? = null,
        footer: String? = null,
        timestamp: TemporalAccessor? = null
    ): EmbedBuilder {
        val embedBuilder = EmbedBuilder()
        embedBuilder.setAuthor(author, authorUrl, authorIcon)
        embedBuilder.setTitle(title, titleUrl)
        embedBuilder.setThumbnail(if (thumbnail == null || thumbnail.isEmpty()) ZiedBot.master.avatarUrl else thumbnail)
        embedBuilder.setDescription(description)
        embedBuilder.setColor(color)
        embedBuilder.setImage(image)
        embedBuilder.setFooter(
            "${if (!footer.isNullOrEmpty()) "$footer  •  " else ""}Powered by Ziedelth.fr \uD83D\uDDA4",
            "https://ziedelth.fr/images/brand.jpg"
        )
        embedBuilder.setTimestamp(timestamp)
        return embedBuilder
    }
}