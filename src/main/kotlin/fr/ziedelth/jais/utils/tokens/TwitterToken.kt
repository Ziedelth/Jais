/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.tokens

data class TwitterToken(
    val OAuthConsumerKey: String? = "",
    val OAuthConsumerSecret: String? = "",
    val OAuthAccessToken: String? = "",
    val OAuthAccessTokenSecret: String? = ""
) : Token {
    override fun isEmpty(): Boolean =
        this.OAuthConsumerKey.isNullOrEmpty() || this.OAuthConsumerSecret.isNullOrEmpty() || this.OAuthAccessToken.isNullOrEmpty() || this.OAuthAccessTokenSecret.isNullOrEmpty()
}
