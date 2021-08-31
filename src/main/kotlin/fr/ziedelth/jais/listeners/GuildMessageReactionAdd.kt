/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.listeners

import fr.ziedelth.jais.utils.ClickType
import fr.ziedelth.jais.utils.reactions
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildMessageReactionAdd : ListenerAdapter() {
    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        super.onGuildMessageReactionAdd(event)
        if (event.user.isBot) return

        reactions["${event.messageIdLong}${event.reactionEmote.asReactionCode}"]?.onClick?.run(
            ClickType.ADD,
            event.userIdLong
        )
    }
}