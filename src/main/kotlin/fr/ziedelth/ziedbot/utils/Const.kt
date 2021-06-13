package fr.ziedelth.ziedbot.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.ziedelth.ziedbot.ZiedBot
import fr.ziedelth.ziedbot.commands.AnimeCommand
import fr.ziedelth.ziedbot.commands.ClearCommand
import fr.ziedelth.ziedbot.commands.HelloCommand
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
import java.time.temporal.TemporalAccessor

object Const {
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    val CHECK_DATE: Check = Check()
    val PLATFORMS: Array<Platform> = arrayOf(Crunchyroll(), AnimeDigitalNetwork(), Wakanim())
    val COMMANDS: Array<Command> = arrayOf(ClearCommand(), HelloCommand(), AnimeCommand())
    val DISCORD_TOKEN: DiscordToken = GSON.fromJson(
        Files.readString(File("tokens", "discord.json").toPath(), StandardCharsets.UTF_8),
        DiscordToken::class.java
    )
    val GUILDS_FOLDER = File("guilds")
        get() {
            if (!field.exists()) field.mkdirs()
            return field
        }

    fun setEmbed(
        author: String?,
        authorUrl: String?,
        authorIcon: String?,
        title: String?,
        titleUrl: String?,
        thumbnail: String?,
        description: String?,
        color: Color?,
        image: String?,
        timestamp: TemporalAccessor?
    ): EmbedBuilder {
        val embedBuilder = EmbedBuilder()
        embedBuilder.setAuthor(author, authorUrl, authorIcon)
        embedBuilder.setTitle(title, titleUrl)
        embedBuilder.setThumbnail(if (thumbnail == null || thumbnail.isEmpty()) ZiedBot.master.avatarUrl else thumbnail)
        embedBuilder.setDescription(description)
        embedBuilder.setColor(color)
        embedBuilder.setImage(image)
        embedBuilder.setFooter("Powered by Ziedelth.fr \uD83D\uDDA4", "https://ziedelth.fr/images/brand.jpg")
        embedBuilder.setTimestamp(timestamp)
        return embedBuilder
    }
}