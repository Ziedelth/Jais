package fr.ziedelth.jais.utils.commands

import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import java.util.*
import java.util.function.Consumer

val buttons: MutableMap<String, CommandButton> = mutableMapOf()

class CommandButton(
    private val style: ButtonStyle,
    private val label: String?,
    private val emoji: Emoji? = null,
    private val url: String? = null,
    private val disabled: Boolean = false,
    val consumer: Consumer<ButtonClickEvent>
) {
    val id: String = UUID.randomUUID().toString()

    init {
        buttons[id] = this
    }

    fun to(): Button = if (url != null && url.isNotEmpty()) Button.of(style, id, label, emoji).withUrl(url)
        .withDisabled(disabled) else Button.of(style, id, label, emoji).withDisabled(disabled)
}