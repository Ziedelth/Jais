/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.debug.JLogger
import java.awt.image.BufferedImage
import java.io.File

object FileImpl {
    private fun exists(file: File): Boolean = file.exists() && if (file.isFile) file.readBytes().isNotEmpty() else true
    private fun notExists(file: File): Boolean = !this.exists(file)

    fun getFile(name: String) = File(File(Jais::class.java.protectionDomain.codeSource.location.path).parent, name)

    private fun createDirectory(file: File) {
        if (this.notExists(file) && !file.mkdirs()) JLogger.warning("Failed to create ${file.name} folder, already exists")
    }

    fun directories(local: Boolean, vararg directories: String): File {
        val folder =
            if (local) getFile(directories.joinToString(File.separator)) else File(directories.joinToString(File.separator))
        Impl.tryCatch("Failed to create $folder folder") { this.createDirectory(folder) }
        return folder
    }

    fun resizeImage(originalImage: BufferedImage?, targetWidth: Int, targetHeight: Int): BufferedImage {
        val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        val graphics2D = resizedImage.createGraphics()
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null)
        graphics2D.dispose()
        return resizedImage
    }
}