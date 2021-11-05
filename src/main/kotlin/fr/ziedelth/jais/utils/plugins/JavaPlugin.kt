/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import java.util.logging.Logger

class JavaPlugin(wrapper: PluginWrapper?) : Plugin(wrapper) {
    private val logger = JPluginLogger(this)

    fun getId(): String = this.wrapper.pluginId
    fun getLogger(): Logger = this.logger

    fun onLoad() {
        this.getLogger().info("${this.getId()} is loaded!")
    }

    fun onEnable() {
        this.getLogger().info("${this.getId()} is enabled!")
    }

    fun onDisable() {
        this.getLogger().info("${this.getId()} is disabled!")
    }
}