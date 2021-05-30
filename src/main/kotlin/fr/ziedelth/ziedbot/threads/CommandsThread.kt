package fr.ziedelth.ziedbot.threads

import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.ZiedLogger
import java.util.*

class CommandsThread : Runnable {
    val thread = Thread(this, "CommandsThread")
    val scanner: Scanner = Scanner(System.`in`)

    init {
        this.thread.isDaemon = true
        this.thread.start()
    }

    override fun run() {
        while (!this.thread.isInterrupted) {
            ZiedLogger.info("Enter a command : ")
            val command: String = this.scanner.nextLine()
            val split: Array<String> = command.split(" ").toTypedArray()
            val subcommand = split[0]
            val args = arrayOfNulls<String>(split.size - 1)
            System.arraycopy(split, 1, args, 0, split.size - 1)

            if (subcommand.equals("setdate", true)) {
                if (args.size != 1) {
                    ZiedLogger.info("Please specify a date")
                    continue
                }

                if (args[0].equals("today", true)) {
                    Const.CHECK_DATE.now = true
                    ZiedLogger.info("Set today date")
                } else {
                    val date: List<String> = args[0]?.split("/") ?: listOf()

                    if (date.size != 3) {
                        ZiedLogger.info("Please specify a correct date format (dd/MM/yyyy)")
                        continue
                    }

                    val calendar: Calendar = Calendar.getInstance()
                    calendar.set(date[2].toInt(), date[1].toInt() - 1, date[0].toInt())
                    Const.CHECK_DATE.now = false
                    Const.CHECK_DATE.calendar = calendar
                    ZiedLogger.info(
                        "Date set to ${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)}/${
                            calendar.get(
                                Calendar.YEAR
                            )
                        }"
                    )
                }
            }
        }
    }
}