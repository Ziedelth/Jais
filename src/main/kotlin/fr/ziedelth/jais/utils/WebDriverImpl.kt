/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.remote.ProtocolHandshake
import org.openqa.selenium.support.ui.WebDriverWait
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

object WebDriverBuilder {
    data class WebDriverImpl(val driver: WebDriver?, val wait: WebDriverWait?)

    private val drivers: MutableList<WebDriverImpl> = mutableListOf()

    fun setDriver(chrome: Boolean = true): WebDriverImpl {
        Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF

        val driver = if (chrome) {
            val options = ChromeOptions()
            options.setCapability("silent", true)
            options.addArguments("--disable-blink-features=AutomationControlled")
            options.addArguments("Referrer")
            options.setExperimentalOption("excludeSwitches", arrayOf("enable-automation"))
            options.setExperimentalOption("useAutomationExtension", false)

            ChromeDriver(ChromeDriverService.Builder().withSilent(true).build(), options)
        } else {
            System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true")
            System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null")
            val options = FirefoxOptions().setProfile(FirefoxProfile())
            options.setCapability("silent", true)
            options.addArguments("--disable-blink-features=AutomationControlled")
            options.addArguments("Referrer")

            FirefoxDriver(options)
        }

        driver.manage().timeouts().pageLoadTimeout(60L, TimeUnit.SECONDS)
        driver.manage().timeouts().setScriptTimeout(60L, TimeUnit.SECONDS)
        val wait = WebDriverWait(driver, 120L)
        val webDriverImpl = WebDriverImpl(driver, wait)

        this.drivers.add(webDriverImpl)
        return webDriverImpl
    }
}
