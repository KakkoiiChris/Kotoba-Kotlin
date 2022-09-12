package kakkoiichris.kotoba

/**
 * Kotoba-Kotlin
 
 * Copyright (C) 2022, KakkoiiChris
 *
 * File:    QuickScript.kt
 
 * Created: Sunday, September 11, 2022, 21:18:28
 *
 * @author Christian Bryce Alexander
 */
class QuickScript(private val source: List<String>) {
    fun run(console: Console): Map<String, String> {
        val vars = mutableMapOf<String, String>()
        
        var newline = true
        
        for (line in source) {
            if (line.startsWith('@')) {
                val tokens = line
                    .substring(0)
                    .lowercase()
                    .split("\\s+".toRegex())
                
                val command = tokens.first()
                val args = tokens.drop(1)
                
                when (command) {
                    "clear"   -> console.clear()
                    
                    "color"   -> when (args.size) {
                        1 -> console.color = args[0].toIntOrNull(16) ?: error("COLOR 1 FAIL")
                        
                        3 -> {
                            val (r, g, b) = args.map { it.toIntOrNull() ?: error("COLOR 3 FAIL") }
                            
                            console.color = (r shl 16) or (g shl 8) or b
                        }
                    }
                    
                    "input"   -> vars[args[0]] = console.readLine() ?: error("INPUT FAIL")
                    
                    "newline" -> newline = args[0].toBooleanStrictOrNull() ?: error("NEWLINE FAIL")
                    
                    "pause"->when (args.size) {
                        0 -> console.pause()
    
                        1 -> console.pause()
                    }
                }
            }
            else {
                if (newline) {
                    console.writeLine(line)
                }
                else {
                    console.write(line)
                }
            }
        }
        
        return vars
    }
}