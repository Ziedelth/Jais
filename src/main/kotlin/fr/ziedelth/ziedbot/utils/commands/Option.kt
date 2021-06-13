package fr.ziedelth.ziedbot.utils.commands

import net.dv8tion.jda.api.interactions.commands.OptionType

class Option(
    val name: String,
    val description: String = "No description...",
    val type: OptionType = OptionType.STRING,
    val required: Boolean = false
)