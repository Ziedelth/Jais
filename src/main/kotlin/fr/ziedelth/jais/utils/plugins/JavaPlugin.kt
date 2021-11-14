/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import fr.ziedelth.jais.utils.animes.sql.data.EpisodeData
import org.pf4j.Plugin
import org.pf4j.PluginWrapper

abstract class JavaPlugin(wrapper: PluginWrapper?) : Plugin(wrapper) {
    abstract fun newEpisode(episodeData: EpisodeData)
}