/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import org.openqa.selenium.WebDriver

object DriverBuilder {
    private val drivers: MutableMap<WebDriver, Long> = mutableMapOf()

    fun addDriver(driver: WebDriver) {
        if (!drivers.containsKey(driver)) drivers[driver] = System.currentTimeMillis()
    }

    fun removeDriver(driver: WebDriver) {
        if (drivers.containsKey(driver)) drivers.remove(driver)
    }

    fun removeAllDeprecatedDrivers() {
        val ids: MutableList<WebDriver> = mutableListOf()
        drivers.filter { (_, timestamp) -> (System.currentTimeMillis() - timestamp) >= 3600000L }
            .forEach { (id, _) -> ids.add(id) }
        ids.forEach { removeDriver(it) }
    }
}