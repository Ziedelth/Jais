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

/* It's a Kotlin's version of the Java formatter. */
object JLogger : Logger("ZiedLogger", null) {
    /* Kotlin's version of the Java formatter */
    class JFormatter : Formatter() {
        private val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.FRANCE)

        /**
         * It formats the log message.
         *
         * @param record The log record.
         * @return The formatted string.
         */
        override fun format(record: LogRecord?): String {
            val message = formatMessage(record)
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            pw.println()
            record?.thrown?.printStackTrace(pw)
            pw.close()
            val throwable: String = sw.toString()
            return "[${this.sdf.format(Date())} ${record?.level?.localizedName}] ${message}${throwable}${if (throwable.isEmpty()) System.lineSeparator() else ""}"
        }
    }

    /* A Kotlin's version of the Java formatter. */
    init {
        val jFormatter = JFormatter()

        this.useParentHandlers = false
        val consoleHandler = ConsoleHandler()
        consoleHandler.formatter = jFormatter
        consoleHandler.level = Level.ALL
        this.addHandler(consoleHandler)
        val logsFolder = FileImpl.getFile("logs")
        if (!logsFolder.exists()) logsFolder.mkdirs()
        val fileHandler = FileHandler("${logsFolder.absolutePath}${File.separator}log.log", 5 * 1024 * 1024, 1, true)
        fileHandler.formatter = jFormatter
        fileHandler.level = Level.ALL
        this.addHandler(fileHandler)
        this.level = Level.ALL
    }
}