/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import com.google.gson.JsonObject
import fr.ziedelth.jais.utils.animes.Country
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
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

private fun getAnimeChannels(country: Country): Array<TextChannel?> = guilds.values.map { ziedGuild ->
    ziedGuild.animeChannels.filter { (_, channel) -> channel.countries.contains(country) }
        .map { (textChannelId, _) -> ziedGuild.guild.getTextChannelById(textChannelId) }
}.flatten().toTypedArray()

fun sendAnimeMessage(country: Country, message: MessageEmbed) {
    getAnimeChannels(country).forEach { it?.sendMessageEmbeds(message)?.submit() }
}

fun sendAnimeMessage(country: Country, message: String) {
    getAnimeChannels(country).forEach { it?.sendMessage(message)?.submit() }
}

fun sendAnimeMessage(country: Country, data: ByteArray, name: String) {
    getAnimeChannels(country).forEach { it?.sendFile(data, name)?.submit() }
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