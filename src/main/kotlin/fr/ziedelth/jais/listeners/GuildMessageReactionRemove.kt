/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.listeners

import fr.ziedelth.jais.utils.ClickType
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.reactions
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildMessageReactionRemove : ListenerAdapter() {
    override fun onGuildMessageReactionRemove(event: GuildMessageReactionRemoveEvent) {
        super.onGuildMessageReactionRemove(event)
        if (event.user?.isBot == true) return

        val id = Const.encodeMD5("${event.messageIdLong}${event.reactionEmote.asReactionCode}")
        if (reactions.containsKey(id)) reactions[id]!!.onClick.run(ClickType.REMOVE, event.userIdLong)
    }
}