/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter

object JLogger : Logger("ZiedLogger", null) {
    init {
        val folder = File("logs")
        if (!folder.exists()) folder.mkdirs()
        val jFormatter = JFormatter()

        this.useParentHandlers = false
        val consoleHandler = ConsoleHandler()
        consoleHandler.formatter = jFormatter
        this.addHandler(consoleHandler)
        val fileHandler = FileHandler("logs/jais-log-%g-%u.log", 5 * 1024 * 1024, 5)
        fileHandler.formatter = jFormatter
        this.addHandler(fileHandler)
    }
}

class JFormatter : Formatter() {
    override fun format(record: LogRecord?): String {
        val message: String = formatMessage(record)
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
        } ${record?.level?.localizedName}] ${message}${throwable}${if (throwable.isEmpty()) System.lineSeparator() else ""}"
    }
}