package kakkoiichris.kotoba.util

import kakkoiichris.kotoba.Glyph

internal val Int.red get() = (this shr 16) and 0xFF

internal val Int.green get() = (this shr 8) and 0xFF

internal val Int.blue get() = this and 0xFF

internal val Int.inverse get() = 0xFFFFFF - this

internal fun blend(srgb: Int, drgb: Int, alpha: Double): Int {
    val sr = srgb.red / 0xFF.toDouble()
    val sg = srgb.green / 0xFF.toDouble()
    val sb = srgb.blue / 0xFF.toDouble()
    
    val dr = drgb.red / 0xFF.toDouble()
    val dg = drgb.green / 0xFF.toDouble()
    val db = drgb.blue / 0xFF.toDouble()
    
    val br = (((alpha * sr) + ((1.0 - alpha) * dr)) * 0xFF).toInt()
    val bg = (((alpha * sg) + ((1.0 - alpha) * dg)) * 0xFF).toInt()
    val bb = (((alpha * sb) + ((1.0 - alpha) * db)) * 0xFF).toInt()
    
    return (br shl 16) or (bg shl 8) or bb
}

internal fun seconds() = System.currentTimeMillis() / 1E3

internal fun millis() = System.currentTimeMillis()

internal fun nanos() = System.nanoTime()

internal fun String.toGlyphs(invert: Boolean, effect: Glyph.Effect) =
    map { Glyph(it, invert, effect) }.toList()