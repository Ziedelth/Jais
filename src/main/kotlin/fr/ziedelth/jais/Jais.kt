package fr.ziedelth.jais

import fr.ziedelth.jais.clients.DiscordClient
import fr.ziedelth.jais.clients.TwitterClient
import fr.ziedelth.jais.threads.AnimeThread
import fr.ziedelth.jais.threads.UpdateThread
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.util.logging.Level
import kotlin.system.exitProcess

object Jais {
    private var isRunning = true

    @JvmStatic
    fun main(args: Array<String>) {
        JLogger.info("Init...")

        try {
            System.setProperty("webdriver.chrome.driver", "/usr/lib/chromium-browser/chromedriver")
            val options = ChromeOptions()
            options.addArguments("--headless")
            options.addArguments("--no-sandbox")
            options.addArguments("--disable-dev-shm-usage")
            val driver = ChromeDriver(options)
            driver.get("https://ziedelth.fr/")
            driver.quit()
        } catch (exception: Exception) {
            JLogger.log(
                Level.WARNING,
                "Can not find Chrome driver, please install it to work... (https://www.google.com/chrome/)",
                exception
            )
            exitProcess(1)
        }

        JLogger.info("Request per day: ${(60L / Const.DELAY_BETWEEN_REQUEST) * 24L}")

        Const.CLIENTS.add(DiscordClient())
        if (Const.PUBLIC) Const.CLIENTS.add(TwitterClient())

        UpdateThread()
        AnimeThread()

        while (isRunning) Thread.sleep(25)
    }
}