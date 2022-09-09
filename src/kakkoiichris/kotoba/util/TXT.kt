package kakkoiichris.kotoba.util

import java.io.*

class TXT(private val filePath: String) {
    val lines = mutableListOf<String>()
    
    val text get() = lines.joinToString(separator = "\n")
    
    private var isResource = false
    
    fun readResource() {
        lines.clear()
        
        lines.addAll(
            javaClass
                .getResourceAsStream(filePath)
                ?.bufferedReader()
                ?.readLines()
                ?: return
        )
        
        isResource = true
    }
    
    fun read() {
        if (!isResource) {
            lines.clear()
            
            val reader = BufferedReader(InputStreamReader(FileInputStream(filePath)))
            
            reader.forEachLine {
                lines.add(it)
            }
            
            reader.close()
        }
    }
    
    fun write() {
        if (!isResource) {
            val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(filePath), "utf-8"))
            
            for (line in lines) {
                writer.write(line)
                writer.newLine()
            }
            
            writer.close()
        }
    }
    
    override fun toString() = text
}