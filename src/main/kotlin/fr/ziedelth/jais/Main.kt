/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

/* It's a Kotlin annotation that tells the compiler to generate a static main method. */
object Main {
    /* It's a Kotlin annotation that tells the compiler to generate a static main method. */
    @JvmStatic
    fun main(args: Array<String>) {
        val jais = Jais()
        jais.init()
    }
}