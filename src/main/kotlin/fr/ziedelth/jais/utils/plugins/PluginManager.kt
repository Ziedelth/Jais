/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import org.pf4j.DefaultPluginManager
import org.pf4j.PluginManager
import java.io.File

object PluginManager {
    private val pluginManager: PluginManager = DefaultPluginManager(File("plugins").toPath())
    val plugins: Array<JavaPlugin>

    init {
        this.pluginManager.loadPlugins()
        this.pluginManager.startPlugins()
        this.plugins =
            this.pluginManager.plugins.filter { it.plugin is JavaPlugin }.map { it.plugin as JavaPlugin }.toTypedArray()
    }

    fun loadPlugins() = this.plugins.forEach { it.onLoad() }
    fun enablePlugins() = this.plugins.forEach { it.onEnable() }
    fun disablePlugins() = this.plugins.forEach { it.onDisable() }
}