package fr.ziedelth.jais.platforms

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.ziedelth.jais.exceptions.EpisodeNotAvailableException
import fr.ziedelth.jais.exceptions.MalformedEpisodeException
import fr.ziedelth.jais.exceptions.UnavailablePlatformException
import fr.ziedelth.jais.utils.*
import java.util.*

class CrunchyrollPlatform : IPlatform("Crunchyroll") {
    private val gson = Gson()

    override fun toEpisode(json: JsonObject): Episode {
        val animeTitle = json.getAsJsonPrimitive("seriesTitle")?.asString ?: throw MalformedEpisodeException("Could not parse anime title")
        // TODO: Parse genres, image and description with url

        val episodeReleaseDate = json.getAsJsonPrimitive("pubDate")?.asString ?: throw MalformedEpisodeException("Could not parse release date")
        val episodeSeason = json.getAsJsonPrimitive("season")?.asString?.toIntOrNull() ?: 1
        val episodeNumber = json.getAsJsonPrimitive("episodeNumber")?.asString?.toIntOrNull() ?: 0
        val episodeId = json.getAsJsonPrimitive("mediaId")?.asString?.toLongOrNull() ?: throw MalformedEpisodeException("Could not parse episode ID")
        val episodeTitle = json.getAsJsonPrimitive("episodeTitle")?.asString
        val episodeUrl = json.getAsJsonObject("guid")?.getAsJsonPrimitive("")?.asString ?: throw MalformedEpisodeException("Could not parse episode URL")
        val episodeImage = json.getAsJsonArray("thumbnail")?.maxByOrNull { it.asJsonObject?.getAsJsonPrimitive("width")?.asString?.toLongOrNull() ?: 0 }?.asJsonObject?.getAsJsonPrimitive("url")?.asString ?: throw MalformedEpisodeException("Could not parse episode image")
        val episodeDuration = json.getAsJsonPrimitive("duration")?.asString?.toLongOrNull() ?: 1420

        // subtitleLanguages, restriction ""
        val subtitleLanguages = json.getAsJsonPrimitive("subtitleLanguages")?.asString ?: throw EpisodeNotAvailableException("Could not parse subtitle languages")
        val restriction = json.getAsJsonObject("restriction")?.getAsJsonPrimitive("")?.asString ?: throw EpisodeNotAvailableException("Could not parse restriction")
        // If subtitleLanguages not contains "fr - fr" or restriction not contains "fr" then throw EpisodeNotAvailableException
        if (!subtitleLanguages.contains("fr - fr") || !restriction.contains("fr")) throw EpisodeNotAvailableException("Episode not available in French")

        return Episode(
            this,
            Anime(animeTitle, "", ""),
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

    override fun getAllEpisodes(calendar: Calendar): Collection<Episode> {
        val networkResponse = Network.connect("https://www.crunchyroll.com/rss/anime?lang=frFR")
        if (!networkResponse.isSuccess) throw UnavailablePlatformException("Could not get episodes from $name (${networkResponse.content})")
        val json = gson.fromJson(ObjectMapper().writeValueAsString(XmlMapper().readTree(networkResponse.content)), JsonObject::class.java)
//        println(json)
        return json.getAsJsonObject("channel").getAsJsonArray("item").mapNotNull { try { toEpisode(it.asJsonObject) } catch (e: Exception) { null } }
    }
}