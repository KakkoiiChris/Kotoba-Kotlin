package kakkoiichris.kotoba

import kakkoiichris.kotoba.util.blend
import kakkoiichris.kotoba.util.inverse
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

class Raster(
    private val width: Int,
    private val height: Int,
    private val pixels: IntArray = IntArray(width * height)
) {
    fun clear(c: Int) {
        pixels.fill(c)
    }

    operator fun get(x: Int, y: Int) =
        if (x in 0 until width && y in 0 until height)
            pixels[x + y * width]
        else
            0

    fun put(x: Int, y: Int, c: Int, a: Double) {
        if (x in 0 until width && y in 0 until height) {
            pixels[x + y * width] = when (a) {
                0.0  -> return

                1.0  -> c

                else -> blend(c, pixels[x + y * width], a)
            }
        }
    }

    fun invertRect(x: Int, y: Int, w: Int, h: Int) {
        for (oy in 0 until h) {
            val yy = y + oy

            if (yy < 0 || yy >= height) continue

            for (ox in 0 until w) {
                val xx = x + ox

                if (xx < 0 || xx >= width) continue

                pixels[xx + yy * width] = pixels[xx + yy * width].inverse
            }
        }
    }

    companion object {
        fun of(image: BufferedImage): Raster {
            val width = image.width
            val height = image.height
            val pixels = (image.raster.dataBuffer as DataBufferInt).data

            return Raster(width, height, pixels)
        }
    }
}