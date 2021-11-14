/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import fr.ziedelth.jais.utils.debug.JLogger
import org.pf4j.Plugin
import org.pf4j.PluginWrapper

abstract class JavaPlugin(wrapper: PluginWrapper?) : Plugin(wrapper) {
    fun getId(): String = this.wrapper.pluginId

    fun onLoad() {
        JLogger.info("${this.getId()} is loaded!")
    }

    fun onEnable() {
        JLogger.info("${this.getId()} is enabled!")
    }

    fun onDisable() {
        JLogger.info("${this.getId()} is disabled!")
    }
}