package fr.ziedelth.ziedbot.utils

import java.util.logging.ConsoleHandler
import java.util.logging.Logger

object ZiedLogger : Logger("ZiedLogger", null) {
    init {
        this.useParentHandlers = false
        val consoleHandler = ConsoleHandler()
        consoleHandler.formatter = ZiedFormatter()
        this.addHandler(consoleHandler)
    }
}