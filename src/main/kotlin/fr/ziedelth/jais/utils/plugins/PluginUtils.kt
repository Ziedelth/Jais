/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.datas.EpisodeType
import kotlin.math.floor

object PluginUtils {
    fun Number.toHHMMSS() = calculateHHMMSS(this.toInt())
    private fun String.onlyLettersAndDigits() = this.filter { it.isLetterOrDigit() }

    private fun calculateHHMMSS(secNum: Int): String {
        val hours = floor(secNum / 3600.0)
        val minutes = floor((secNum - (hours * 3600.0)) / 60.0)
        val seconds = secNum - (hours * 3600.0) - (minutes * 60.0)
        var h: String = hours.toInt().toString()
        var m: String = minutes.toInt().toString()
        var s: String = seconds.toInt().toString()

        if (hours < 10) h = "0${hours.toInt()}"
        if (minutes < 10) m = "0${minutes.toInt()}"
        if (seconds < 10) s = "0${seconds.toInt()}"

        return "${(if (hours >= 1) "$h:" else "")}$m:$s"
    }

    fun getMessage(episode: Episode) = "🎉 ${episode.anime}\n" +
            "${getEpisodeTitle(episode)}\n" +
            "${getEpisodeDataMessage(episode)} ${episode.langType.getData(episode.country.country::class.java)?.data}\n" +
            "${getEpisodeTimeMessage(episode)}\n" +
            "#Anime #${episode.anime.onlyLettersAndDigits()} #${episode.platform.platformHandler.name.onlyLettersAndDigits()}"

    fun getMarkdownMessage(episode: Episode) = "**${getEpisodeTitle(episode)}**\n" +
            getEpisodeDataMessage(episode) +
            getEpisodeTimeMessage(episode)

    private fun getEpisodeTitle(episode: Episode) = episode.title ?: "＞﹏＜"

    private fun getEpisodeTimeMessage(episode: Episode) =
        "🎬 ${if (episode.duration != -1L) episode.duration.toHHMMSS() else "??:??"}"

    private fun getEpisodeDataMessage(episode: Episode) =
        "${episode.country.countryHandler.season} ${episode.season} • ${episode.episodeType.getData(episode.country.country::class.java)?.data}${if (episode.episodeType != EpisodeType.EPISODE) "" else " ${episode.number}"}\n"
}