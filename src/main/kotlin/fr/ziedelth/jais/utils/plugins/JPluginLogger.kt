/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter

class JPluginLogger(val plugin: JavaPlugin) : Logger("ZiedPluginLogger", null) {
    init {
        val jPluginFormatter = JPluginFormatter(this.plugin)

        this.useParentHandlers = false
        val consoleHandler = ConsoleHandler()
        consoleHandler.formatter = jPluginFormatter
        consoleHandler.level = Level.ALL
        this.addHandler(consoleHandler)
        val logsFolder = File("logs")
        if (!logsFolder.exists()) logsFolder.mkdirs()
        val fileHandler = FileHandler("logs/log.log", 1 * 1024 * 1024, 5, true)
        fileHandler.formatter = jPluginFormatter
        fileHandler.level = Level.ALL
        this.addHandler(fileHandler)
        this.level = Level.ALL
    }
}

class JPluginFormatter(val plugin: JavaPlugin) : Formatter() {
    override fun format(record: LogRecord?): String {
        val message = formatMessage(record)
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        pw.println()
        record?.thrown?.printStackTrace(pw)
        pw.close()
        val throwable: String = sw.toString()
        return "[${
            SimpleDateFormat(
                "HH:mm:ss yyyy/MM/dd",
                Locale.FRANCE
            ).format(Date())
        } ${record?.level?.localizedName}] [${this.plugin.getId()}] ${message}${throwable}${if (throwable.isEmpty()) System.lineSeparator() else ""}"
    }
}