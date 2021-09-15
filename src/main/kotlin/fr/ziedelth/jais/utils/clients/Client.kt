/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.clients

import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News
import org.pf4j.Plugin
import org.pf4j.PluginWrapper

abstract class Client(wrapper: PluginWrapper?) : Plugin(wrapper) {
    abstract fun sendEpisodes(episodes: Array<Episode>)
    open fun sendNews(news: Array<News>) {}
}

