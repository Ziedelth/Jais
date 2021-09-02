/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import fr.ziedelth.jais.threads.AnimeThread
import fr.ziedelth.jais.threads.UpdateThread
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.database.JAccess
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
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
            JLogger.log(Level.WARNING, "Can not connect to database, please check config...", exception)
            exitProcess(1)
        }

        JLogger.info("Testing selenium...")
        try {
            val driver =
                ChromeDriver(ChromeDriverService.Builder().withSilent(true).build(), ChromeOptions().setHeadless(true))
            driver.get("https://ziedelth.fr/")
            driver.quit()
            JLogger.info("Selenium work!")
        } catch (exception: Exception) {
            JLogger.log(
                Level.WARNING,
                "Can not find Chrome driver, please install it to work... (https://www.google.com/chrome/)",
                exception
            )
            exitProcess(1)
        }

        JLogger.info("Request per day: ${(60L / Const.DELAY_BETWEEN_REQUEST) * 24L}")

        JLogger.info("Activating client(s)...")
        // SWITCH TO PLUGINS

        JLogger.info("Enable all threads...")
        UpdateThread()
        AnimeThread()

        JLogger.info("Running...")
        while (isRunning) Thread.sleep(25)
    }
}