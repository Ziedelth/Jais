package fr.ziedelth.jais.utils

import java.util.*

abstract class IPlatform(val name: String) {
    abstract fun toEpisode(episode: String): Episode
    open fun getAllEpisodes(calendar: Calendar = Calendar.getInstance()) {}
}