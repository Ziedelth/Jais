package fr.ziedelth.ziedbot.utils

import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Formatter
import java.util.logging.LogRecord

class ZiedFormatter : Formatter() {
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