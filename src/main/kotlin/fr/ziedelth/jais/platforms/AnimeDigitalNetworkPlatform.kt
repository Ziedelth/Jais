package fr.ziedelth.jais.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.jais.exceptions.MalformedEpisodeException
import fr.ziedelth.jais.utils.*
import java.util.*

class AnimeDigitalNetworkPlatform : IPlatform("Anime Digital Network") {
    override fun toEpisode(episode: String): Episode {
        val json = try {
            Gson().fromJson(episode, JsonObject::class.java)
        } catch (e: Exception) {
            null
        } ?: throw MalformedEpisodeException("Could not parse JSON")

        val showJson = json.getAsJsonObject("show")
        val animeTitle = showJson.get("originalTitle").asString ?: showJson.get("shortTitle").asString
        ?: showJson.get("title").asString ?: throw MalformedEpisodeException("Could not parse anime title")
        val animeDescription = showJson.get("summary").asString
        val animeImage =
            showJson.getAsJsonPrimitive("image2x").asString ?: showJson.getAsJsonPrimitive("image").asString
            ?: throw MalformedEpisodeException("Could not parse anime image")

        val episodeReleaseDate = json.getAsJsonPrimitive("releaseDate").asString
            ?: throw MalformedEpisodeException("Could not parse release date")
        val episodeSeason = json.getAsJsonPrimitive("season").asString.toIntOrNull() ?: 1
        val episodeNumber = json.getAsJsonPrimitive("shortNumber").asString.toIntOrNull() ?: 0
        val episodeId = json.getAsJsonPrimitive("id").asString.toLongOrNull()
            ?: throw MalformedEpisodeException("Could not parse episode ID")
        val episodeTitle = json.getAsJsonPrimitive("name").asString
        val episodeUrl =
            json.getAsJsonPrimitive("url").asString ?: throw MalformedEpisodeException("Could not parse episode URL")
        val episodeImage = json.getAsJsonPrimitive("image2x").asString ?: json.getAsJsonPrimitive("image").asString
        ?: throw MalformedEpisodeException("Could not parse episode image")
        val episodeDuration = json.getAsJsonPrimitive("duration").asString.toLongOrNull()
            ?: throw MalformedEpisodeException("Could not parse episode duration")

        return Episode(
            this,
            Anime(animeTitle, animeDescription, animeImage),
            episodeReleaseDate,
            episodeSeason,
            episodeNumber,
            episodeId.toString(),
            episodeTitle,
            episodeUrl,
            episodeImage,
            episodeDuration
        )
    }

    override fun getAllEpisodes(calendar: Calendar) {
        val networkResponse =
            Network.connect("https://gw.api.animedigitalnetwork.fr/video/calendar?date=${calendar.toISODate()}")

        if (!networkResponse.isSuccess) {
            println("Can not get episodes from Anime Digital Network: ${networkResponse.content}")
            return
        }

        println("Episodes: ${networkResponse.content}")
    }
}