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
    private val profile = FirefoxProfile()
    private val service = ChromeDriverService.Builder().withSilent(true).build() as ChromeDriverService

    fun setDriver(chrome: Boolean = true, show: Boolean = false): WebDriverImpl {
        Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF

        if (chrome) {
            val options = ChromeOptions().setHeadless(!show)
            options.addArguments("--disable-blink-features=AutomationControlled")
            options.setExperimentalOption("excludeSwitches", arrayOf("enable-automation"))
            options.setExperimentalOption("useAutomationExtension", false)
            options.setBinary("C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe")

            val driver = ChromeDriver(this.service, options)
            driver.manage().timeouts().pageLoadTimeout(60L, TimeUnit.SECONDS)
            driver.manage().timeouts().setScriptTimeout(60L, TimeUnit.SECONDS)
            val wait = WebDriverWait(driver, 120L)
            return WebDriverImpl(driver, wait)
        } else {
            System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true")
            System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null")
            val options = FirefoxOptions().setHeadless(!show).setProfile(this.profile)
            options.setCapability("silent", true)
            options.addArguments("--disable-blink-features=AutomationControlled")

            val driver = FirefoxDriver(options)
            driver.manage().timeouts().pageLoadTimeout(60L, TimeUnit.SECONDS)
            driver.manage().timeouts().setScriptTimeout(60L, TimeUnit.SECONDS)
            val wait = WebDriverWait(driver, 120L)
            return WebDriverImpl(driver, wait)
        }
    }
}

data class WebDriverImpl(val driver: WebDriver?, val wait: WebDriverWait?)
