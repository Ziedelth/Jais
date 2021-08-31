/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.tokens

data class InstagramToken(val username: String? = "", val password: String? = "") : Token {
    override fun isEmpty(): Boolean = this.username.isNullOrEmpty() || this.password.isNullOrEmpty()
}
