package fr.ziedelth.ziedbot.utils.commands

import net.dv8tion.jda.api.entities.Command

class Option(
    val name: String,
    val description: String = "No description...",
    val type: Command.OptionType = Command.OptionType.STRING,
    val required: Boolean = false
)