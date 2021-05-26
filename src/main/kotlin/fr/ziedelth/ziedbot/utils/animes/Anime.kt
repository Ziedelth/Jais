package fr.ziedelth.ziedbot.utils.animes

class Anime(val name: String, private val episodes: MutableList<Episode> = mutableListOf()) {
    fun contains(string: String): Boolean = this.episodes.any { it.toString() == string }
    fun contains(episode: Episode): Boolean = this.contains(episode.toString())

    fun get(string: String): Episode? = this.episodes.firstOrNull { it.toString() == string }
    fun get(episode: Episode): Episode? = this.get(episode.toString())

    fun add(episode: Episode): Boolean {
        if (!this.contains(episode)) {
            this.episodes.add(episode)
            return true
        }

        return false
    }
}