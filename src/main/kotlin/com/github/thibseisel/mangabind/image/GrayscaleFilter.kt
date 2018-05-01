package com.github.thibseisel.mangabind.image

import java.awt.image.BufferedImage

/**
 * Transforms an input image so that color are converted to gray scales.
 */
class GrayscaleFilter {

    fun apply(image: BufferedImage) {
        val width = image.width
        val height = image.height

        for (i in 0 until height) {
            for (j in 0 until width) {
                val color = image.getRGB(j, i)
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = (color shl 0) and 0xFF
                val gray = (r * 0.299 + g * 0.587 + b * 0.114).toInt()
                val newColor = (gray shl 16) + (gray shl 8) + gray
                image.setRGB(j, i, newColor)
            }
        }
    }
}