/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import fr.ziedelth.jais.threads.AnimeThread
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.clients.Client
import fr.ziedelth.jais.utils.database.JAccess
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.pf4j.DefaultPluginManager
import org.pf4j.PluginManager
import java.io.File
import java.util.logging.Level
import kotlin.system.exitProcess

object Jais {
    private var isRunning = true

    @JvmStatic
    fun main(args: Array<String>) {
        JLogger.info("Init...")

        JLogger.info("Testing connection to the database...")
        try {
            JAccess.getConnection()?.close()
            JLogger.info("Connected with the database!")
        } catch (exception: Exception) {
            JLogger.log(Level.SEVERE, "Can not connect to database, please check config...", exception)
            exitProcess(1)
        }

        JLogger.info("Testing selenium...")
        try {
            val driver =
                ChromeDriver(ChromeDriverService.Builder().withSilent(true).build(), ChromeOptions().setHeadless(true))
            driver.get("https://google.com/")
            driver.quit()
            JLogger.info("Selenium work!")
        } catch (exception: Exception) {
            JLogger.log(
                Level.SEVERE,
                "Can not find Chrome driver, please install it to work... (https://www.google.com/chrome/)",
                exception
            )
            exitProcess(1)
        }

        JLogger.info("Request per day: ${(60L / Const.DELAY_BETWEEN_REQUEST) * 24L}")

        JLogger.info("Activating client(s)...")
        // SWITCH TO PLUGINS
        val pluginManager: PluginManager = DefaultPluginManager(File("plugins").toPath())
        pluginManager.loadPlugins()
        pluginManager.startPlugins()
        pluginManager.plugins.filter { it.plugin is Client }.forEach { Const.CLIENTS.add(it.plugin as Client) }

        JLogger.info("Enable all threads...")
        AnimeThread()

        JLogger.info("Running...")
        while (isRunning) Thread.sleep(25)
    }
}