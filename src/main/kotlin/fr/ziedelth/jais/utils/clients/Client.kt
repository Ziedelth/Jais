/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.clients

import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News

interface Client {
    fun update()

    fun sendEpisodes(episodes: Array<Episode>) {}
    fun sendNews(news: Array<News>) {}
}

