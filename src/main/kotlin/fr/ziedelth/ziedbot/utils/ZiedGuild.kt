package fr.ziedelth.ziedbot.utils

import com.google.gson.JsonObject
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import java.io.File
import java.nio.file.Files

fun Guild.getZiedGuild(): ZiedGuild = get(this)

val guilds: MutableMap<Long, ZiedGuild> = mutableMapOf()

private fun get(guild: Guild): ZiedGuild {
    return if (guilds.containsKey(guild.idLong)) guilds[guild.idLong]!!
    else {
        val ziedGuild = ZiedGuild(guild)
        guilds[guild.idLong] = ziedGuild
        ziedGuild
    }
}

class ZiedGuild(private val guild: Guild) {
    private val file = File(Const.GUILDS_FOLDER, "${guild.idLong}.json")
    var animeChannel: TextChannel? = null
        set(value) {
            field = value
            save()
        }

    init {
        load()
    }

    private fun load() {
        if (file.exists()) {
            val jsonObject = Const.GSON.fromJson(Files.readString(file.toPath()), JsonObject::class.java)
            animeChannel = guild.getTextChannelById(jsonObject["anime-channel"]?.asLong ?: 0)
        }
    }

    private fun save() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("anime-channel", animeChannel?.idLong ?: 0)
        Files.writeString(file.toPath(), Const.GSON.toJson(jsonObject))
    }
}