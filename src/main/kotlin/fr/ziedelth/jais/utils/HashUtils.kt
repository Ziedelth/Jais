/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import java.security.MessageDigest

object HashUtils {
    /**
     * Compute the SHA-512 hash of a given String
     *
     * @param input The string you want to hash.
     */
    fun sha512(input: String?) = hashString("SHA-512", input)

    /**
     * Compute the SHA-256 hash of a given String and return the result as a hexadecimal String
     *
     * @param input The string to hash.
     */
    fun sha256(input: String) = hashString("SHA-256", input)

    /**
     * Compute the SHA-1 hash of the input string and return the hash as a hex string
     *
     * @param input The string to hash.
     */
    fun sha1(input: String) = hashString("SHA-1", input)


    /**
     * It takes a string and returns a string
     *
     * @param type The type of hash to use.
     * @param input The string to be hashed.
     * @return The hash string.
     */
    private fun hashString(type: String, input: String?): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input?.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}