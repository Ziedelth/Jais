package fr.ziedelth.jais.utils.clients

import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News

interface Client {
    fun getJClient(): JClient
    fun saveJClient(jClient: JClient)

    fun update()
    fun sendEpisode(episodes: Array<Episode>, new: Boolean)
    fun sendNews(news: Array<News>)
}