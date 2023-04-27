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

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Frame
import java.awt.Image
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.imageio.ImageIO

class Console(config: Config = Config()) {
    private val frame = Frame(config.title)
    
    private val buffer = Buffer(config)
    
    private var closed = true
    
    private val promptStack = Stack<String>()
    var prompt: String; private set
    
    var title by frame::title
    
    var icon by frame::iconImage
    
    var color: Int
        get() = (buffer.effect as? Glyph.Effect.Color)?.rgb ?: -1
        set(value) {
            buffer.effect = Glyph.Effect.Color(value)
        }
    
    var effect by buffer::effect
    
    var invert by buffer::invert
    
    var rulesEnabled by buffer::rulesEnabled
    
    val isOpen get() = frame.isVisible
    
    init {
        frame.layout = BorderLayout()
        frame.add(buffer, BorderLayout.CENTER)
        frame.isFocusable = false
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.focusTraversalKeysEnabled = false
        frame.iconImage = config.icon
        frame.background = Color(config.background)
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                close()
            }
        })
        
        prompt = config.prompt
    }
    
    fun pushPrompt(prompt: String?) {
        promptStack.push(prompt)
        
        this.prompt = java.lang.String.join("", promptStack)
    }
    
    fun popPrompt() {
        if (!promptStack.isEmpty()) {
            promptStack.pop()
        }
        
        prompt = promptStack.joinToString(separator = "")
    }
    
    fun setPrompt(prompt: String?) {
        if (!promptStack.isEmpty()) {
            promptStack.pop()
        }
        
        promptStack.push(prompt)
        
        this.prompt = promptStack.joinToString(separator = "")
    }
    
    fun open() {
        if (!closed) {
            return
        }
        
        closed = false
        
        frame.isVisible = true
        
        buffer.open()
    }
    
    fun close() {
        if (closed) {
            return
        }
        
        closed = true
        
        buffer.close()
        
        frame.dispose()
    }
    
    fun addRules(vararg rules: Glyph.Rule) = buffer.addRules(*rules)
    
    fun hasRule(name: String) = buffer.hasRule(name)
    
    fun getRule(name: String) = buffer.getRule(name)
    
    fun getRuleOrNull(name: String) = buffer.getRuleOrNull(name)
    
    fun removeRules(vararg names: String) = buffer.removeRules(*names)
    
    fun clearRules() = buffer.clearRules()
    
    fun clear() {
        if (closed) {
            return
        }
        
        buffer.clear()
    }
    
    fun pause(seconds: Double = 0.0) {
        if (closed) {
            return
        }
        
        if (seconds == 0.0) {
            buffer.write("Press enter to continue...")
            
            while (true) {
                if ((readKey(true) ?: return).keyCode == KeyEvent.VK_ENTER) {
                    return
                }
            }
        }
        
        Thread.sleep((seconds * 1000).toLong())
    }
    
    fun readKey(onPress: Boolean): KeyEvent? {
        if (closed) {
            return null
        }
        
        return buffer.readKey(onPress)
    }
    
    fun readToken(): String? {
        if (closed) {
            return null
        }
        
        return buffer.readToken()
    }
    
    fun readLine(): String? {
        if (closed) {
            return null
        }
        
        return buffer.readLine()
    }
    
    fun readOption(vararg options: String): String? {
        if (closed) {
            return null
        }
        
        for ((i, option) in options.withIndex()) {
            buffer.write("(${i + 1}) $option\n")
        }
        
        var choice: Int
        
        do {
            buffer.write("> ")
            
            val input = buffer.readLine()
            
            if (input.toIntOrNull() == null) {
                buffer.write("Please enter a number.\n")
                continue
            }
            
            choice = input.toInt() - 1
            
            if (choice !in options.indices) {
                buffer.write("Please enter a valid choice.\n")
                continue
            }
            
            break
        }
        while (true)
        
        return options[choice]
    }
    
    fun write(x: Any?) {
        if (closed) {
            return
        }
        
        buffer.write("$x")
    }
    
    fun writeFormat(format: String, vararg args: Any?) {
        if (closed) {
            return
        }
        
        buffer.write(String.format(format, args))
    }
    
    fun writeLine(x: Any? = "") {
        if (closed) {
            return
        }
        
        buffer.write("$x\n")
    }
    
    data class Config(
        val title: String = "Kotoba - Dynamic RGB ASCII Console",
        val icon: Image = ImageIO.read(Config::class.java.getResourceAsStream("/img/icon.png")),
        val width: Int = 800,
        val height: Int = 600,
        val foreground: Int = Glyph.Effect.Color.white.rgb,
        val background: Int = Glyph.Effect.Color.black.rgb,
        val font: Font = Font("/font/Fixedsys16.bff"),
        val xSpace: Int = 0,
        val ySpace: Int = 0,
        val tabSize: Int = 4,
        val frameRate: Double = 60.0,
        val scrollSpeed: Double = 0.25,
        val scrollAmount: Int = 1,
        val scrollBarWidth: Int = 8,
        val cursorSpeed: Double = 0.5,
        val inputDelimiter: String = " ",
        val prompt: String = "",
    )
}