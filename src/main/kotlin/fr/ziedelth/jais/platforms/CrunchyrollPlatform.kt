package fr.ziedelth.jais.platforms

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.ziedelth.jais.exceptions.MalformedEpisodeException
import fr.ziedelth.jais.exceptions.UnavailablePlatformException
import fr.ziedelth.jais.utils.*
import java.util.*

class CrunchyrollPlatform : IPlatform("Crunchyroll") {
    private val gson = Gson()

    override fun toEpisode(json: JsonObject): Episode {
        val showJson = json.getAsJsonObject("show")
        val animeTitle = showJson.get("originalTitle").asString ?: showJson.get("shortTitle").asString ?: showJson.get("title").asString ?: throw MalformedEpisodeException("Could not parse anime title")
        val animeDescription = showJson.get("summary").asString
        val animeImage = showJson.getAsJsonPrimitive("image2x").asString ?: showJson.getAsJsonPrimitive("image").asString ?: throw MalformedEpisodeException("Could not parse anime image")
        val animeGenres: JsonArray = showJson.getAsJsonArray("genres") ?: throw MalformedEpisodeException("Could not parse anime genres")
        if (!animeGenres.map { it.asString }.contains("Animation japonaise")) throw MalformedEpisodeException("Anime is not an anime")

        val episodeReleaseDate = json.getAsJsonPrimitive("releaseDate").asString ?: throw MalformedEpisodeException("Could not parse release date")
        val episodeSeason = json.getAsJsonPrimitive("season").asString.toIntOrNull() ?: 1
        val episodeNumber = json.getAsJsonPrimitive("shortNumber").asString.toIntOrNull() ?: 0
        val episodeId = json.getAsJsonPrimitive("id").asString.toLongOrNull() ?: throw MalformedEpisodeException("Could not parse episode ID")
        val episodeTitle = json.getAsJsonPrimitive("name").asString
        val episodeUrl = json.getAsJsonPrimitive("url").asString ?: throw MalformedEpisodeException("Could not parse episode URL")
        val episodeImage = json.getAsJsonPrimitive("image2x").asString ?: json.getAsJsonPrimitive("image").asString ?: throw MalformedEpisodeException("Could not parse episode image")
        val episodeDuration = json.getAsJsonPrimitive("duration").asString.toLongOrNull() ?: throw MalformedEpisodeException("Could not parse episode duration")

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

    override fun getAllEpisodes(calendar: Calendar): Collection<Episode> {
        val networkResponse = Network.connect("https://www.crunchyroll.com/rss/anime?lang=frFR")
        if (!networkResponse.isSuccess) throw UnavailablePlatformException("Could not get episodes from $name")
        val json = gson.fromJson(ObjectMapper().writeValueAsString(XmlMapper().readTree(networkResponse.content)), JsonObject::class.java)
        println(json)
        // return json.getAsJsonArray("videos").mapNotNull { try { toEpisode(it.asJsonObject) } catch (e: Exception) { null } }
        return arrayListOf()
    }
}