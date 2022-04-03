/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import fr.ziedelth.jais.Jais
import java.awt.image.BufferedImage
import java.io.File

/* It's a class. */
object FileImpl {
    /* It's getting the current folder of the jar file. */
    private val currentFolder = File(Jais::class.java.protectionDomain.codeSource.location.path).parent

    /* It's checking if the file exists and if it's a file, it's checking if it's not empty. */
    private fun exists(file: File): Boolean = file.exists() && if (file.isFile) file.readBytes().isNotEmpty() else true

    /**
     * Check if the file does not exist.
     *
     * @param file The file to check.
     */
    private fun notExists(file: File): Boolean = !this.exists(file)

    /**
     * "Check if a file with the given name exists in the current folder."
     *
     * The function is declared as a top-level function, which means it can be called from anywhere in the program
     *
     * @param name The name of the file to be created.
     */
    fun fileExists(name: String) = File(this.currentFolder, name).exists()

    /**
     * Create a File object with the given name in the current folder
     *
     * @param name The name of the file to be created.
     */
    fun getFile(name: String) = File(this.currentFolder, name)

    /**
     * If the file doesn't exist, create it
     *
     * @param file The file to create.
     */
    private fun createDirectory(file: File) {
        if (this.notExists(file) && !file.mkdirs()) JLogger.warning("Failed to create ${file.name} folder, already exists")
    }

    /**
     * If the local flag is true, then the function creates a folder in the current directory. Otherwise, it creates a
     * folder in the specified directory
     *
     * @param local Boolean,
     * @param directories The directories to create.
     * @return Nothing.
     */
    fun directories(local: Boolean, vararg directories: String): File {
        val folder =
            if (local) getFile(directories.joinToString(File.separator)) else File(directories.joinToString(File.separator))
        Impl.tryCatch("Failed to create $folder folder") { this.createDirectory(folder) }
        return folder
    }

    /**
     * Create a new image with the given width and height, and draw the given image on it
     *
     * @param originalImage The image to be resized.
     * @param targetWidth The width of the resized image.
     * @param targetHeight The height of the resized image.
     * @return Nothing is being returned.
     */
    fun resizeImage(originalImage: BufferedImage?, targetWidth: Int, targetHeight: Int): BufferedImage {
        val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        val graphics2D = resizedImage.createGraphics()
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null)
        graphics2D.dispose()
        return resizedImage
    }
}