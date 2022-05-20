package fr.ziedelth.jais.utils

import java.text.SimpleDateFormat
import java.util.*

private val ISO_DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd")

fun Calendar.toISODate(): String = ISO_DATE_FORMATTER.format(this.time)