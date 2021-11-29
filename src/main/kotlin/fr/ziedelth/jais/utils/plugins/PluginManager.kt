/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import fr.ziedelth.jais.utils.FileImpl
import org.pf4j.DefaultPluginManager
import org.pf4j.PluginManager

object PluginManager {
    private val pluginManager: PluginManager = DefaultPluginManager(FileImpl.getFile("plugins").toPath())
    var plugins: Array<JavaPlugin>? = null

    fun loadAll() {
        this.pluginManager.loadPlugins()
        this.pluginManager.startPlugins()
        this.plugins =
            this.pluginManager.plugins.filter { it.plugin is JavaPlugin }.map { it.plugin as JavaPlugin }.toTypedArray()
    }
}