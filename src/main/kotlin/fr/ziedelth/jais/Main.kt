/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.JThread

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
//        Jais()
        JThread.startMultiThread({
            println("test 1")
        }, {
            println("test 2")
        }, {
            println("test 3")
        }, {
            println("test 4")
        })
        JThread.stopAll()

        println("Has internet : ${Impl.hasInternet()}")
    }
}