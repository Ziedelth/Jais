package fr.ziedelth.ziedbot.utils

import com.google.gson.JsonObject
import fr.ziedelth.ziedbot.utils.animes.Country
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
    var animeChannels: MutableMap<String, MutableList<Country>> = mutableMapOf()

    init {
        load()
    }

    private fun load() {
        if (file.exists()) {
            val jsonObject =
                Const.GSON.fromJson(Files.readString(file.toPath(), Const.DEFAULT_CHARSET), JsonObject::class.java)

            if (jsonObject.has(KEY_ANIME_CHANNELS) && !jsonObject[KEY_ANIME_CHANNELS].isJsonNull) {
                val acObject = jsonObject[KEY_ANIME_CHANNELS].asJsonObject

                acObject.entrySet().forEach { entry ->
                    val countriesArray = entry.value.asJsonArray
                    val countries: MutableList<Country> = mutableListOf()
                    countriesArray.forEach { countries.add(Country.valueOf(it.asString)) }
                    this.animeChannels[entry.key] = countries
                }
            }
        }
    }

    fun save() {
        val jsonObject = JsonObject()
        jsonObject.add(KEY_ANIME_CHANNELS, Const.GSON.toJsonTree(this.animeChannels))
        Files.writeString(file.toPath(), Const.GSON.toJson(jsonObject), Const.DEFAULT_CHARSET)
    }
}