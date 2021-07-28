package fr.ziedelth.jais.utils

import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News

interface Client {
    fun update()
    fun sendEpisode(episodes: Array<Episode>, new: Boolean)
    fun sendNews(news: Array<News>)
}