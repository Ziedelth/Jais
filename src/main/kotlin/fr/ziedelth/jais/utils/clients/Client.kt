/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.clients

import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News

interface Client {
    fun getJClient(): JClient
    fun saveJClient(jClient: JClient)
    fun update()

    fun sendNewEpisodes(episodes: Array<Episode>) {
        TODO()
    }

    fun sendEditEpisodes(episodes: Array<Episode>) {
        TODO()
    }

    fun sendNews(news: Array<News>)
}

