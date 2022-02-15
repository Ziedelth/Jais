/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.platforms

data class PlatformImpl(val platformHandler: PlatformHandler, val platform: Platform) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlatformImpl

        if (platformHandler != other.platformHandler) return false
        if (platform != other.platform) return false

        return true
    }

    override fun hashCode(): Int {
        var result = platformHandler.hashCode()
        result = 31 * result + platform.hashCode()
        return result
    }
}
