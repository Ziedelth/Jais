/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import net.dv8tion.jda.api.entities.Message

val reactions: MutableMap<String, Reaction> = mutableMapOf()

fun removeAllReactionsFrom(message: Message) {
    val ids = reactions.filter { (_, reaction) -> reaction.message.idLong == message.idLong }.map { (id, _) -> id }
    ids.forEach { reactions.remove(it) }
}

fun removeAllDeprecatedReactions() {
    val ids =
        reactions.filter { (_, reaction) -> reaction.deprecatedWithTime && (System.currentTimeMillis() - reaction.timestamp) >= 3600000L }
            .map { (id, _) -> id }
    ids.forEach { reactions.remove(it) }
}

class Reaction(
    val timestamp: Long = System.currentTimeMillis(),
    val deprecatedWithTime: Boolean = true,
    val message: Message,
    val unicode: String,
    val onClick: ClickRunnable
) {
    val id = "${this.message.idLong}${this.unicode}"

    fun add(action: Runnable? = null) {
        if (!reactions.containsKey(this.id)) {
            reactions[this.id] = this
            this.message.addReaction(this.unicode).queue { action?.run() }
        }
    }

    fun remove() {
        if (reactions.containsKey(this.id)) {
            this.message.removeReaction(this.unicode).queue()
            reactions.remove(this.id)
        }
    }
}