/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.tokens

data class DatabaseToken(
    val url: String? = "",
    val user: String? = "",
    val password: String? = ""
) : Token {
    override fun isEmpty(): Boolean =
        this.url.isNullOrEmpty() || this.user.isNullOrEmpty() || this.password.isNullOrEmpty()
}
