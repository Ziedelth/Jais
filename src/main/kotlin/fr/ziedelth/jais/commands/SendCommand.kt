/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.commands

import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.commands.Command
import fr.ziedelth.jais.utils.commands.CommandHandler
import fr.ziedelth.jais.utils.plugins.PluginManager
import kotlin.system.exitProcess

@CommandHandler(command = "send", description = "Send a message to plugins")
class SendCommand : Command {
    override fun onCommand(args: List<String>) {
        val message = args.joinToString(" ")

        JLogger.info("Message: $message")
        PluginManager.sendMessage(message)
    }
}
