package fr.ziedelth.jais.utils

import com.google.gson.JsonObject
import java.util.*

abstract class IPlatform(val name: String) {
    abstract fun toEpisode(json: JsonObject): Episode
    open fun getAllEpisodes(calendar: Calendar = Calendar.getInstance()): Collection<Episode> = emptyList()
}