/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.tokens

data class DiscordToken(val token: String? = "") : Token {
    override fun isEmpty(): Boolean = this.token.isNullOrEmpty()
}
