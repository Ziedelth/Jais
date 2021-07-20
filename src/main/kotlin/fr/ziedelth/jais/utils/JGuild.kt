package fr.ziedelth.jais.utils

import com.google.gson.JsonObject
import net.dv8tion.jda.api.entities.Guild
import java.io.File
import java.nio.file.Files

fun Guild.getJGuild(): JGuild = get(this)

val guilds: MutableMap<Long, JGuild> = mutableMapOf()

private fun get(guild: Guild): JGuild {
    return if (guilds.containsKey(guild.idLong)) guilds[guild.idLong]!!
    else {
        val ziedGuild = JGuild(guild)
        guilds[guild.idLong] = ziedGuild
        ziedGuild
    }
}

class JGuild(val guild: Guild) {
    private val keyAnimeChannels = "anime-channels"

    private val file = File(Const.GUILDS_FOLDER, "${guild.idLong}.json")
    var animeChannels: MutableMap<String, Channel> = mutableMapOf()

    init {
        load()
    }

    private fun load() {
        if (file.exists()) {
            val jsonObject =
                Const.GSON.fromJson(Files.readString(file.toPath(), Const.DEFAULT_CHARSET), JsonObject::class.java)

            if (jsonObject.has(keyAnimeChannels) && !jsonObject[keyAnimeChannels].isJsonNull) {
                val acObject = jsonObject[keyAnimeChannels].asJsonObject
                acObject.entrySet().forEach { entry ->
                    this.animeChannels[entry.key] = Const.GSON.fromJson(entry.value, Channel::class.java)
                }
            }
        }
    }

    fun save() {
        val jsonObject = JsonObject()
        jsonObject.add(keyAnimeChannels, Const.GSON.toJsonTree(this.animeChannels))
        Files.writeString(file.toPath(), Const.GSON.toJson(jsonObject), Const.DEFAULT_CHARSET)
    }
}