/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.commands

import fr.ziedelth.jais.utils.commands.Command
import fr.ziedelth.jais.utils.commands.CommandHandler
import kotlin.system.exitProcess

@CommandHandler(command = "exit")
class ExitCommand : Command {
    override fun onCommand(args: List<String>) {
        exitProcess(0)
    }
}
