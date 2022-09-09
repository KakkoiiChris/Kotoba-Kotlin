/*  _     _       _       _   ____        _
 * | |   |_|     | |     | | |  _ \      |_|
 * | |    _  ___ | |__  _| |_| | | | ____ _ _   _  ___
 * | |   | |/ _ \|  _ \|_   _| | | |/ ___| | \ / |/ _ \
 * | |___| | |_| | | | | | | | |_| | |   | |\ V /|  ___|
 * |_____|_|\__  |_| |_| |_| |____/|_|   |_| \_/  \___|
 *  _____   ___| |  ___________________________________
 * |_____| |____/  |_________JAVA_GAME_LIBRARY_________|
 *
 * COPYRIGHT (C) 2015, CHRISTIAN BRYCE ALEXANDER
 */
package kakkoiichris.kotoba.util

import java.io.*

class CSV(private val filePath: String) {
    private val rows = mutableListOf<Row>()
    
    private var isResource = false
    
    fun readResource() {
        rows.clear()
        
        val lines = javaClass
            .getResourceAsStream(filePath)
            ?.bufferedReader()
            ?.readLines()
            ?: return
        
        rows.addAll(lines.map { Row.parse(it) })
        
        isResource = true
    }
    
    fun read() {
        if (!isResource) {
            rows.clear()
            
            val reader = BufferedReader(InputStreamReader(FileInputStream(filePath)))
            
            reader.forEachLine {
                rows.add(Row.parse(it))
            }
            
            reader.close()
        }
    }
    
    fun write() {
        if (!isResource) {
            val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(filePath), "utf-8"))
            
            for (row in rows) {
                writer.write(row.toString())
                writer.newLine()
            }
            
            writer.close()
        }
    }
    
    operator fun get(index: Int) =
        rows[index]
    
    class Row(val data: List<String>) : List<String> by data {
        companion object {
            fun parse(line: String): Row {
                val fullLine = line + '\u0000'
                val data = mutableListOf<String>()
                
                var token = ""
                
                var inSingleQuotes = false
                var inDoubleQuotes = false
                
                for (char in fullLine) {
                    if (!inDoubleQuotes && char == '\'') {
                        inSingleQuotes = !inSingleQuotes
                        
                        continue
                    }
                    
                    if (!inSingleQuotes && char == '"') {
                        inDoubleQuotes = !inDoubleQuotes
                        
                        continue
                    }
                    
                    if (!inSingleQuotes && !inDoubleQuotes && (char == ',' || char == '\u0000')) {
                        data.add(token)
                        
                        token = ""
                        
                        continue
                    }
                    
                    token += char
                }
                
                return Row(data)
            }
        }
        
        val header get() = data[0]
        
        val dataWithoutHeader get() = data.drop(1)
        
        override fun toString() = data.joinToString(separator = ",") {
            var s = it.toString()
            
            if (',' in s) {
                s = if ('\'' in s) {
                    "\"$s\""
                }
                else {
                    "'$s'"
                }
            }
            
            s
        }
    }
}