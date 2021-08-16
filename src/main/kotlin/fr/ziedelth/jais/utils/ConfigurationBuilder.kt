/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import fr.ziedelth.jais.utils.animes.Country
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import kotlin.math.min

private val configurations: MutableMap<Long, ConfigurationBuilder> = mutableMapOf()

fun hasConfiguration(textChannel: TextChannel) = configurations.contains(textChannel.idLong)
fun hasConfiguration(configurationBuilder: ConfigurationBuilder) =
    configurations.containsKey(configurationBuilder.textChannel.idLong)

fun getConfiguration(textChannel: TextChannel) = configurations[textChannel.idLong]
fun addConfiguration(configurationBuilder: ConfigurationBuilder) {
    configurations[configurationBuilder.textChannel.idLong] = configurationBuilder
}

fun removeConfiguration(textChannel: TextChannel) {
    configurations.remove(textChannel.idLong)
}

fun removeAllDeprecatedConfigurations() {
    val ids: MutableList<Long> = mutableListOf()
    configurations.filter { (_, configuration) -> configuration.deprecatedWithTime && (System.currentTimeMillis() - configuration.timestamp) >= 3600000L }
        .forEach { (id, _) -> ids.add(id) }
    ids.forEach { configurations.remove(it) }
}

class ConfigurationBuilder(
    val timestamp: Long = System.currentTimeMillis(),
    val deprecatedWithTime: Boolean = true,
    val user: User,
    val textChannel: TextChannel,
    var configurationStep: ConfigurationStep = ConfigurationStep.UNKNOWN
) {
    var page: Int = 1
    val countries: MutableList<Country> = mutableListOf()
    var anime = true
    var news = true

    fun addCountry(country: Country) {
        if (!this.countries.contains(country)) this.countries.add(country)
    }

    fun removeCountry(country: Country) {
        if (this.countries.contains(country)) this.countries.remove(country)
    }

    fun getCountriesPerPage() = Country.values()
        .slice(((this.page - 1) * Const.DISPLAY)..min(Country.values().size - 1, (this.page * Const.DISPLAY)))

    fun getMaxPagesDisplay() =
        if ((Country.values().size / Const.DISPLAY) > 1) ((Country.values().size / Const.DISPLAY) + 1) else 1

    fun nextStep() {
        this.page = 1

        this.configurationStep = when (this.configurationStep) {
            ConfigurationStep.COUNTRIES -> ConfigurationStep.ANIME
            ConfigurationStep.ANIME -> ConfigurationStep.NEWS
            ConfigurationStep.NEWS -> ConfigurationStep.FINISH
            else -> ConfigurationStep.UNKNOWN
        }
    }
}

enum class ConfigurationStep {
    UNKNOWN,
    COUNTRIES,
    ANIME,
    NEWS,
    FINISH,
    ;
}