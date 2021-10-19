/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.ProtocolHandshake
import org.openqa.selenium.support.ui.WebDriverWait
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

object WebDriverBuilder {

    private val service = ChromeDriverService.Builder().withSilent(true).build() as ChromeDriverService

    fun setDriver(show: Boolean = false): WebDriverImpl {
        val options = ChromeOptions().setHeadless(!show).setProxy(null)

        Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF
        val driver = ChromeDriver(this.service, options)
        driver.manage().timeouts().pageLoadTimeout(60L, TimeUnit.SECONDS)
        driver.manage().timeouts().setScriptTimeout(60L, TimeUnit.SECONDS)
        val wait = WebDriverWait(driver, 10L)
        return WebDriverImpl(driver, wait)
    }
}

data class WebDriverImpl(val driver: ChromeDriver?, val wait: WebDriverWait?)
