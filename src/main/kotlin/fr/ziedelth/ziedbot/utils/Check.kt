package fr.ziedelth.ziedbot.utils

import java.util.*

class Check(var now: Boolean = true, var calendar: Calendar = Calendar.getInstance()) {
    fun getDate(): Calendar = if (this.now) Calendar.getInstance() else calendar
}