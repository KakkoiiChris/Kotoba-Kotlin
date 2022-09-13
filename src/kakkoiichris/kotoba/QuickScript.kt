package kakkoiichris.kotoba

import java.util.regex.Pattern

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
                    .substring(1)
                    .split("\\s+".toRegex())
                
                val command = tokens.first().lowercase()
                val args = tokens.drop(1)
                
                when (command) {
                    "clear"           -> console.clear()
                    
                    "color"           -> when (args.size) {
                        1 -> console.color = args[0].toIntOrNull(16) ?: error("COLOR 1 FAIL")
                        
                        3 -> {
                            val (r, g, b) = args.map { it.toIntOrNull() ?: error("COLOR 3 FAIL") }
                            
                            console.color = (r shl 16) or (g shl 8) or b
                        }
                    }
                    
                    "input"           -> vars[args[0]] = console.readLine() ?: error("INPUT FAIL")
                    
                    "invert"          -> console.invert = args[0].toBooleanStrictOrNull() ?: error("INVERT FAIL")
                    
                    "newline"         -> newline = args[0].toBooleanStrictOrNull() ?: error("NEWLINE FAIL")
                    
                    "pause"           -> when (args.size) {
                        0 -> console.pause()
                        
                        1 -> console.pause(args[0].toDoubleOrNull() ?: error("PAUSE 1 FAIL"))
                    }
                    
                    "rule"            -> {
                        val (name, regex, invert) = args
                        
                        val rule = Glyph.Rule(
                            name,
                            regex.toRegex(),
                            invert.toBooleanStrictOrNull() ?: error("RULE INVERT FAIL"),
                            Glyph.Effect.None
                        )
                        
                        console.addRules(rule)
                    }
                    
                    "rule_color"      -> {
                        val (name, color) = args
                        
                        val rule = console.getRuleOrNull(name) ?: error("RULE_COLOR NO RULE")
                        
                        val effect = Glyph.Effect.Color(color.toIntOrNull(16) ?: error("RULE_COLOR COLOR FAIL"))
                        
                        rule.effect = effect
                    }
                    
                    "rule_and_color"  -> {
                        val (name, color) = args
                        
                        val rule = console.getRuleOrNull(name) ?: error("RULE_AND_COLOR NO RULE")
                        
                        val effect = Glyph.Effect.Color(color.toIntOrNull(16) ?: error("RULE_AND_COLOR COLOR FAIL"))
                        
                        rule.effect = rule.effect and effect
                    }
                    
                    "rule_cycle"      -> {
                        val (name, speed) = args
                        
                        val colors = args.drop(2).map { it.toIntOrNull(16) ?: error("RULE_CYCLE COLORS NOT HEX") }.toIntArray()
                        
                        val rule = console.getRuleOrNull(name) ?: error("RULE_CYCLE NO RULE")
                        
                        val effect = Glyph.Effect.Cycle(
                            speed.toDoubleOrNull() ?: error("RULE_CYCLE SPEED FAIL"),
                            *colors
                        )
                        
                        rule.effect = effect
                    }
                    
                    "rule_and_cycle"  -> {
                        val (name, speed) = args
                        
                        val colors = args.drop(2).map { it.toIntOrNull(16) ?: error("RULE_AND_CYCLE COLORS NOT HEX") }.toIntArray()
                        
                        val rule = console.getRuleOrNull(name) ?: error("RULE_AND_CYCLE NO RULE")
                        
                        val effect = Glyph.Effect.Cycle(
                            speed.toDoubleOrNull() ?: error("RULE_AND_CYCLE SPEED FAIL"),
                            *colors
                        )
                        
                        rule.effect = rule.effect and effect
                    }
                    
                    "rule_random"     -> {
                        val (name) = args
                        
                        val rule = console.getRuleOrNull(name) ?: error("RULE_RANDOM NO RULE")
                        
                        rule.effect = Glyph.Effect.Random
                    }
                    
                    "rule_and_random" -> {
                        val (name) = args
                        
                        val rule = console.getRuleOrNull(name) ?: error("RULE_AND_RANDOM NO RULE")
                        
                        rule.effect = rule.effect and Glyph.Effect.Random
                    }
                    
                    "rule_jitter"     -> {
                        val (name, x, y) = args
                        
                        val rule = console.getRuleOrNull(name) ?: error("RULE_JITTER NO RULE")
                        
                        val effect = Glyph.Effect.Jitter(
                            x.toIntOrNull() ?: error("RULE_JITTER X FAIL"),
                            y.toIntOrNull() ?: error("RULE_JITTER Y FAIL")
                        )
                        
                        rule.effect = effect
                    }
                    
                    "rule_and_jitter" -> {
                        val (name, x, y) = args
                        
                        val rule = console.getRuleOrNull(name) ?: error("RULE_AND_JITTER NO RULE")
                        
                        val effect = Glyph.Effect.Jitter(
                            x.toIntOrNull() ?: error("RULE_AND_JITTER X FAIL"),
                            y.toIntOrNull() ?: error("RULE_AND_JITTER Y FAIL")
                        )
                        
                        rule.effect = rule.effect and effect
                    }
                    
                    "rule_wave"       -> {
                        val (name, amplitude, frequency, speed, vertical) = args
                        
                        val rule = console.getRuleOrNull(name) ?: error("RULE_WAVE NO RULE")
                        
                        val effect = Glyph.Effect.Wave(
                            amplitude.toDoubleOrNull() ?: error("RULE_WAVE AMPLITUDE FAIL"),
                            frequency.toDoubleOrNull() ?: error("RULE_WAVE FREQUENCY FAIL"),
                            speed.toDoubleOrNull() ?: error("RULE_WAVE SPEED FAIL"),
                            vertical.toBooleanStrictOrNull() ?: error("RULE_WAVE VERTICAL FAIL"),
                        )
                        
                        rule.effect = effect
                    }
                    
                    "rule_and_wave"   -> {
                        val (name, amplitude, frequency, speed, vertical) = args
                        
                        val rule = console.getRuleOrNull(name) ?: error("RULE_AND_WAVE NO RULE")
                        
                        val effect = Glyph.Effect.Wave(
                            amplitude.toDoubleOrNull() ?: error("RULE_AND_WAVE AMPLITUDE FAIL"),
                            frequency.toDoubleOrNull() ?: error("RULE_AND_WAVE FREQUENCY FAIL"),
                            speed.toDoubleOrNull() ?: error("RULE_AND_WAVE SPEED FAIL"),
                            vertical.toBooleanStrictOrNull() ?: error("RULE_AND_WAVE VERTICAL FAIL"),
                        )
                        
                        rule.effect = rule.effect and effect
                    }
                    
                    "rule_remove"->console.removeRules(args[0])
                    
                    "rules"           -> console.rulesEnabled = args[0].toBooleanStrictOrNull() ?: error("RULES FAIL")
                    
                    "rules_clear"->console.clearRules()
                }
            }
            else {
                var output = line
                
                val pattern = Pattern.compile("\\{(\\w+)}")
                
                while (true) {
                    val matcher = pattern.matcher(output)
                    
                    if (!matcher.find()) break
                    
                    val name = matcher.group(1)
                    
                    output = matcher.replaceFirst(vars[name] ?: error("NO VAR FOR MATCHER"))
                }
                
                if (newline) {
                    console.writeLine(output)
                }
                else {
                    console.write(output)
                }
            }
        }
        
        return vars
    }
}