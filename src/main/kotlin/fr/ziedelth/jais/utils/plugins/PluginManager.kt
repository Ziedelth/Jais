/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News
import org.pf4j.DefaultPluginManager
import org.pf4j.PluginManager

/* It's a way to create a singleton. */
object PluginManager {
    /* Creating a new instance of the plugin manager. */
    private val pluginManager: PluginManager = DefaultPluginManager(FileImpl.getFile("plugins").toPath())

    /* It's a way to store the plugins in a variable. */
    var plugins: Array<JavaPlugin>? = null

    /**
     * Load all plugins and start them
     */
    fun loadAll() {
        this.pluginManager.loadPlugins()
        this.pluginManager.startPlugins()
        this.plugins =
            this.pluginManager.plugins.filter { it.plugin is JavaPlugin }.map { it.plugin as JavaPlugin }.toTypedArray()
        JLogger.config("Loaded ${this.plugins!!.size} plugins. (${this.plugins!!.joinToString(", ") { it.wrapper.pluginId }})")
    }

    fun sendEpisode(episode: Episode) {
        this.plugins?.forEach {
            Impl.tryCatch("Can not send episode for ${it.wrapper.pluginId} plugin") {
                it.newEpisode(episode)
            }
        }
    }

    fun sendNews(news: News) {
        this.plugins?.forEach {
            Impl.tryCatch("Can not send news for ${it.wrapper.pluginId} plugin") {
                it.newNews(news)
            }
        }
    }

    fun sendMessage(message: String) {
        this.plugins?.forEach {
            Impl.tryCatch("Can not send message for ${it.wrapper.pluginId} plugin") {
                it.sendMessage(message)
            }
        }
    }
}