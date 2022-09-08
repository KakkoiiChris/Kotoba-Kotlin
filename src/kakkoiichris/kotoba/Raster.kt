package kakkoiichris.kotoba

import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

class Raster(image: BufferedImage) {
    private val pixels = (image.raster.dataBuffer as DataBufferInt).data
    private val width = image.width
    private val height = image.height
    
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
                
                pixels[xx + yy * width] = 0xFFFFFF - pixels[xx + yy * width]
            }
        }
    }
    
    fun clear(c: Int) {
        pixels.fill(c)
    }
}