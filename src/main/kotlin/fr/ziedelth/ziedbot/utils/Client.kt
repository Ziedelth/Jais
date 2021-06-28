package fr.ziedelth.ziedbot.utils

import fr.ziedelth.ziedbot.utils.animes.Episode
import fr.ziedelth.ziedbot.utils.animes.News

interface Client {
    fun sendEpisode(episodes: Array<Episode>, new: Boolean)
    fun sendNews(news: Array<News>)
}