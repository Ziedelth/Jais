/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import fr.ziedelth.jais.utils.debug.JLogger
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.math.pow

object FileImpl {
    fun exists(file: File): Boolean = file.exists() && if (file.isFile) file.readBytes().isNotEmpty() else true
    fun notExists(file: File): Boolean = !this.exists(file)

    private fun createFile(file: File) {
        if (this.notExists(file) && !file.createNewFile()) JLogger.warning("Failed to create ${file.name} file, already exists")
    }

    private fun createDirectory(file: File) {
        if (this.notExists(file) && !file.mkdirs()) JLogger.warning("Failed to create ${file.name} folder, already exists")
    }

    fun directories(vararg directories: String): File {
        val folder = File(directories.joinToString(File.separator))
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

    fun compressImage(image: BufferedImage, outputStream: OutputStream, quality: Float) {
        val writer = ImageIO.getImageWritersByFormatName("jpg").next()
        val ios = ImageIO.createImageOutputStream(outputStream)
        writer.output = ios

        val param = writer.defaultWriteParam

        if (param.canWriteCompressed()) {
            param.compressionMode = ImageWriteParam.MODE_EXPLICIT
            param.compressionQuality = quality
        }

        writer.write(null, IIOImage(image, null, null), param)
        outputStream.close()
        ios.close()
        writer.dispose()
    }

    fun toFormat(bytes: Long): String {
        return if (bytes.toFloat() > 0.8 * 1024.0.pow(3.0).toFloat()) String.format(
            "%.2f GiB",
            bytes.toFloat() / 1024.0.pow(3.0).toFloat()
        )
        else if (bytes.toFloat() > 0.8 * 1024.0.pow(2.0).toFloat()) String.format(
            "%.2f MiB",
            bytes.toFloat() / 1024.0.pow(2.0).toFloat()
        )
        else if (bytes.toFloat() > 0.8 * 1024.0.pow(1.0).toFloat()) String.format(
            "%.2f KiB",
            bytes.toFloat() / 1024.0.pow(1.0).toFloat()
        )
        else String.format("%.0f B", bytes.toFloat())
    }

    fun toFormat(bytes: Int): String = toFormat(bytes.toLong())
}