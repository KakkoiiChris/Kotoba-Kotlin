/*******************************************************
 * ██╗  ██╗ ██████╗ ████████╗ ██████╗ ██████╗  █████╗  *
 * ██║ ██╔╝██╔═══██╗╚══██╔══╝██╔═══██╗██╔══██╗██╔══██╗ *
 * █████╔╝ ██║   ██║   ██║   ██║   ██║██████╔╝███████║ *
 * ██╔═██╗ ██║   ██║   ██║   ██║   ██║██╔══██╗██╔══██║ *
 * ██║  ██╗╚██████╔╝   ██║   ╚██████╔╝██████╔╝██║  ██║ *
 * ╚═╝  ╚═╝ ╚═════╝    ╚═╝    ╚═════╝ ╚═════╝ ╚═╝  ╚═╝ *
 *              DYNAMIC RGB ASCII CONSOLE              *
 *                                                     *
 *    Copyright (C) 2039, Innovolt Fabrications Inc.   *
 *******************************************************/
package kakkoiichris.kotoba

import java.io.DataInputStream

class Font(path: String) {
    private val rows: Int
    private val cols: Int
    val height: Int
    
    private val firstChar: Char
    
    private val chars: Array<DoubleArray>
    
    init {
        fun Int.fixed(): Int {
            val byte0 = (this shr 24) and 0xFF
            val byte1 = (this shr 16) and 0xFF
            val byte2 = (this shr 8) and 0xFF
            val byte3 = this and 0xFF
            
            return (byte3 shl 24) or (byte2 shl 16) or (byte1 shl 8) or byte0
        }
        
        val data = DataInputStream(
            javaClass
                .getResourceAsStream(path)
                ?.buffered()
                ?: error("Font @ '$path' unable to load!")
        )
        
        // Skip Format Bitmap Font File Version ID
        data.readShort()
        
        val fontImageWidth = data.readInt().fixed()
        val fontImageHeight = data.readInt().fixed()
        
        val cellWidth = data.readInt().fixed()
        val cellHeight = data.readInt().fixed()
        
        rows = fontImageHeight / cellHeight
        cols = fontImageWidth / cellWidth
        
        height = cellHeight
        
        // Skip bytes-per-pixel, always 8-bit grayscale
        data.readByte()
        
        firstChar = data.readUnsignedByte().toChar()
        
        val characterWidths = IntArray(256) {
            data.readUnsignedByte()
        }
        
        val allValues = Array(fontImageHeight) {
            DoubleArray(fontImageWidth) {
                data.readUnsignedByte() / 0xFF.toDouble()
            }
        }
        
        var row = 0
        var col = 0
        
        val charValues = mutableListOf<DoubleArray>()
        
        for (char in firstChar..0xFF.toChar()) {
            val values = mutableListOf<Double>()
            
            val width = characterWidths[char.toInt()]
            
            values += width.toDouble()
            
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val vr = (row * cellHeight) + y
                    val vc = (col * cellWidth) + x
                    
                    values += allValues[vr][vc]
                }
            }
            
            if (++col == cols) {
                col = 0
                row++
            }
            
            charValues += values.toDoubleArray()
        }
        
        this.chars = charValues.toTypedArray()
    }
    
    operator fun get(char: Char): Pair<Int, DoubleArray> {
        val charData = if (char >= firstChar) {
            chars[char - firstChar]
        }
        else {
            chars[' ' - firstChar]
        }
        
        return charData[0].toInt() to charData.drop(1).toDoubleArray()
    }
    
    fun widthOfGlyphs(glyphs: List<Glyph>, space: Int): Int {
        var fullWidth = 0
        
        val iterator = glyphs.iterator()
        
        while (iterator.hasNext()) {
            val (char, _) = iterator.next()
            
            val (width, _) = get(char)
            
            fullWidth += width + space
        }
        
        return fullWidth
    }
    
    fun widthOfChars(chars: List<Char>, space: Int): Int {
        var fullWidth = 0
        
        val iterator = chars.iterator()
        
        while (iterator.hasNext()) {
            val char = iterator.next()
            
            val (width, _) = get(char)
            
            fullWidth += width + space
        }
        
        return fullWidth
    }
}