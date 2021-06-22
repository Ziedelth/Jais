package fr.ziedelth.ziedbot.utils

import com.google.gson.JsonObject
import fr.ziedelth.ziedbot.utils.animes.Language
import net.dv8tion.jda.api.entities.Guild
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

class ZiedGuild(val guild: Guild) {
    private val KEY_ANIME_CHANNELS = "anime-channels"

    private val file = File(Const.GUILDS_FOLDER, "${guild.idLong}.json")
    var animeChannels: MutableMap<Language, String> = mutableMapOf()

    init {
        load()
    }

    private fun load() {
        if (file.exists()) {
            val jsonObject = Const.GSON.fromJson(Files.readString(file.toPath()), JsonObject::class.java)

            if (jsonObject.has(KEY_ANIME_CHANNELS) && !jsonObject[KEY_ANIME_CHANNELS].isJsonNull) {
                val acObject = jsonObject[KEY_ANIME_CHANNELS].asJsonObject

                acObject.entrySet().forEach { entry ->
                    if (Language.values().any { it.name == entry.key }) this.animeChannels[Language.values()
                        .find { it.name == entry.key }!!] = entry.value.asString
                }
            }
        }
    }

    fun save() {
        val jsonObject = JsonObject()
        jsonObject.add(KEY_ANIME_CHANNELS, Const.GSON.toJsonTree(this.animeChannels))
        Files.writeString(file.toPath(), Const.GSON.toJson(jsonObject))
    }
}