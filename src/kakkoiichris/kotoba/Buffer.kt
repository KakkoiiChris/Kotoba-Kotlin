package kakkoiichris.kotoba

import kakkoiichris.kotoba.util.nanos
import kakkoiichris.kotoba.util.seconds
import kakkoiichris.kotoba.util.toGlyphs
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.event.*
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max
import kotlin.math.min

class Buffer(config: Console.Config) : Canvas(), Runnable, KeyListener, MouseWheelListener {
    private val foreground = config.foreground
    private val background = config.background
    private val font = config.font
    private val xSpace = config.xSpace
    private val ySpace = config.ySpace
    private val tabSize = config.tabSize
    private val frameRate = config.frameRate
    private val scrollSpeed = config.scrollSpeed
    private val scrollBarWidth = config.scrollBarWidth
    private val cursorSpeed = config.cursorSpeed
    private val inputDelimiter = config.inputDelimiter
    
    private var image: BufferedImage
    private var raster: Raster
    
    // Output
    private val output = mutableListOf<Glyph>()
    private val outputBuffer = mutableListOf<Glyph>()
    private val outputLock = ReentrantLock()
    
    // Input
    private val input = mutableListOf<Glyph>()
    private val inputBuffer = mutableListOf<Glyph>()
    private val inputQueue = ArrayBlockingQueue<String>(1)
    private val inputLock = ReentrantLock()
    private var inputIndex = 0
    private var inputWaiting = false
    private val inputScanBuffer = mutableListOf<String>()
    private val inputHistory = mutableListOf<String>()
    private var inputHistoryIndex = -1
    
    // Keys
    private val keyQueue = ArrayBlockingQueue<KeyEvent>(1)
    private var keyWaiting = false
    private var keyOnPress = false
    
    // Cursor
    private var cursorBlinkTimer = seconds()
    private var cursorVisible = false
    
    // Formatting
    private val rules = mutableMapOf<String, Glyph.Rule>()
    internal var rulesEnabled = true
    internal var effect: Glyph.Effect = Glyph.Effect.Color(foreground)
    internal var invert = false
    
    // Update Loop
    private val thread = Thread(this)
    private var running = false
    
    // Scroll Bar
    private val scrollBarMargin = 4
    private val scrollBarBounds = Rectangle2D.Double()
    private var scrollTarget = 0
    private var scrollOffset = 0.0
    
    private val waiting get() = inputWaiting || keyWaiting
    private val lineCount get() = output.count { it.char == '\n' } + 1
    private val linesOnScreen get() = height / (font.height + ySpace)
    
    init {
        preferredSize = Dimension(config.width, config.height)
        
        image = BufferedImage(config.width, config.height, BufferedImage.TYPE_INT_RGB)
        
        raster = Raster(image)
        
        addKeyListener(this)
        addMouseWheelListener(this)
        
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                image = BufferedImage(this@Buffer.width, this@Buffer.height, BufferedImage.TYPE_INT_RGB)
                
                raster = Raster(image)
                
                end()
            }
        })
    }
    
    fun open() {
        requestFocus()
        
        thread.start()
    }
    
    fun close() {
        inputQueue.put("")
        keyQueue.put(KeyEvent(this, 0, 0, 0, KeyEvent.VK_ENTER, '\n'))
        
        running = false
    }
    
    fun addRules(vararg rules: Glyph.Rule) {
        for (rule in rules) {
            this.rules[rule.name] = rule
        }
    }
    
    fun hasRule(name: String) =
        name in rules.keys
    
    fun removeRules(vararg names: String) {
        for (name in names) {
            rules.remove(name)
        }
    }
    
    fun clear() {
        outputLock.withLock {
            output.clear()
            outputBuffer.clear()
        }
    }
    
    fun readKey(onPress: Boolean): KeyEvent {
        keyWaiting = true
        
        keyOnPress = onPress
        
        val key = keyQueue.take()
        
        keyWaiting = false
        
        return key
    }
    
    fun read(): String {
        if (inputScanBuffer.isEmpty()) {
            inputWaiting = true
            
            val line = inputQueue.take()
            
            val tokens = line.split(inputDelimiter)
            
            inputScanBuffer.addAll(tokens)
            
            inputWaiting = false
        }
        
        val token = inputScanBuffer.removeAt(0)
        
        if (token.isNotBlank() && (inputHistory.isEmpty() || token != inputHistory[0])) {
            inputHistory.add(0, token)
        }
        
        return token
    }
    
    fun readText(): String {
        inputWaiting = true
        
        val line = inputQueue.take()
        
        inputWaiting = false
        
        if (line.isNotBlank() && (inputHistory.isEmpty() || line != inputHistory[0])) {
            inputHistory.add(0, line)
        }
        
        return line
    }
    
    fun write(string: String) {
        val matches = mutableMapOf<Glyph.Rule, List<IntRange>>()
        
        if (rulesEnabled) {
            for (rule in rules.values) {
                val ranges = mutableListOf<IntRange>()
                
                matches[rule] = ranges
                
                val matcher = rule.regex.toPattern().matcher(string)
                
                while (matcher.find()) {
                    val group = matcher.toMatchResult()
                    
                    ranges.add(group.start() until group.end())
                }
            }
        }
        
        outputLock.withLock {
            for ((i, char) in string.withIndex()) {
                var thisEffect = effect
                var thisInvert = invert
                
                rules@ for ((rule, ranges) in matches) {
                    for (range in ranges) {
                        if (i !in range) {
                            continue
                        }
                        
                        thisEffect = rule.effect
                        thisInvert = rule.invert
                        
                        break@rules
                    }
                }
                
                outputBuffer += Glyph(char, thisEffect.copy(), thisInvert)
            }
        }
    }
    
    override fun run() {
        val npu = 1E9 / frameRate
        
        var delta = 0.0
        var timer = 0.0
        
        var then = nanos()
        
        var updates = 0
        var frames = 0
        
        running = true
        
        while (running) {
            val now = nanos()
            val elapsed = (now - then) / npu
            then = now
            
            delta += elapsed
            timer += elapsed
            
            var changed = false
            
            while (delta >= 1.0) {
                update(delta--)
                
                updates++
                
                changed = true
            }
            
            if (changed) {
                render()
                
                frames++
            }
            
            poll()
            
            if (timer >= frameRate) {
                println("$updates U, $frames F")
                
                updates = 0
                frames = 0
                
                timer -= frameRate
            }
        }
    }
    
    private fun update(delta: Double) {
        output.forEach { glyph -> glyph.update(delta) }
        
        if (inputWaiting) {
            input.forEach { it.update(delta) }
        }
        
        if (seconds() - cursorBlinkTimer >= cursorSpeed) {
            cursorVisible = (waiting && !cursorVisible) || false
            
            cursorBlinkTimer += cursorSpeed
        }
        
        scrollOffset += (scrollTarget - scrollOffset) * scrollSpeed
    }
    
    private fun render() {
        renderRaster()
        
        if (bufferStrategy == null) {
            createBufferStrategy(3)
        }
        
        val graphics = bufferStrategy.drawGraphics as Graphics2D
        
        graphics.drawImage(image, 0, 0, width, height, null)
        
        graphics.color = Color(255, 255, 255, 127)
        
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        val scrollBarMargin = scrollBarWidth / 2
        val scrollBarMaxHeight = height - scrollBarMargin * 2
        val scrollBarHeight =
            (scrollBarMaxHeight * (linesOnScreen.toDouble() / max(lineCount, linesOnScreen))).toInt()
        val sbX = width - scrollBarWidth - scrollBarMargin
        val sbY = (scrollBarMargin + (scrollBarMaxHeight - scrollBarHeight) * (scrollOffset / (lineCount - linesOnScreen))).toInt()
        
        graphics.fillRoundRect(sbX, sbY, scrollBarWidth, scrollBarHeight, scrollBarWidth, scrollBarWidth)
        
        graphics.dispose()
        
        bufferStrategy.show()
    }
    
    @Suppress("DestructuringWrongName")
    private fun renderRaster() {
        raster.clear(background)
        
        var ox = xSpace
        var oy = ySpace - (scrollOffset * (font.height + ySpace)).toInt()
        
        outputLock.withLock {
            val iterator = output.iterator()
            
            while (iterator.hasNext()) {
                val (char, invert, color, jx, jy) = iterator.next()
                
                when (char) {
                    '\t' -> {
                        val (width, _) = font[' ']
                        
                        ox += (width + xSpace) * tabSize
                        
                        continue
                    }
                    
                    '\n' -> {
                        ox = xSpace
                        oy += font.height + ySpace
                        
                        continue
                    }
                }
                
                val gx = ox + jx
                val gy = oy + jy
                
                val (width, values) = font[char]
                
                for (y in 0 until font.height) {
                    for (x in 0 until width) {
                        val px = gx + x
                        val py = gy + y
                        
                        val value = values[x + y * width]
                        
                        val alpha = if (invert) 1.0 - value else value
                        
                        raster.put(px, py, color, alpha)
                    }
                }
                
                ox += width + xSpace
            }
        }
        
        if (inputWaiting) {
            for ((i, glyph) in input.withIndex()) {
                val (char, invert, color, jx, jy) = glyph
                
                val gx = ox + jx
                val gy = oy + jy
                
                val (width, values) = font[char]
                
                for (y in 0 until font.height) {
                    for (x in 0 until width) {
                        val px = gx + x
                        val py = gy + y
                        
                        val value = values[x + y * width]
                        
                        val alpha = if (invert || (i == inputIndex && cursorVisible)) 1.0 - value else value
                        
                        raster.put(px, py, color, alpha)
                    }
                }
                
                ox += width + xSpace
            }
            
            if ((input.isEmpty() || inputIndex !in input.indices) && cursorVisible) {
                val (width) = font[' ']
                
                raster.invertRect(ox, oy, width, font.height)
            }
        }
        
        if (keyWaiting && cursorVisible) {
            val (width, values) = font['A']
            
            for (y in 0 until font.height) {
                for (x in 0 until width) {
                    val px = ox + x
                    val py = oy + y
                    
                    val alpha = 1.0 - values[x + y * width]
                    
                    raster.put(px, py, foreground, alpha)
                }
            }
        }
    }
    
    private fun poll() {
        inputLock.withLock {
            val iterator = inputBuffer.iterator()
            
            while (iterator.hasNext()) {
                val glyph = iterator.next()
                
                if (glyph.char.isISOControl()) when (glyph.char) {
                    '\b'     -> if (inputIndex - 1 in input.indices) {
                        input.removeAt(inputIndex-- - 1)
                    }
                    else {
                        beep()
                    }
                    
                    '\u007F' -> if (inputIndex in input.indices) {
                        input.removeAt(inputIndex)
                    }
                    else {
                        beep()
                    }
                }
                else {
                    input.add(inputIndex++, glyph)
                }
                
                iterator.remove()
            }
        }
        
        outputLock.withLock {
            val iterator = outputBuffer.iterator()
            
            val outputWritten = iterator.hasNext()
            
            while (iterator.hasNext()) {
                val glyph = iterator.next()
                
                output += glyph
                
                iterator.remove()
            }
            
            if (outputWritten) {
                end()
            }
        }
    }
    
    private fun blinkCursor() {
        cursorBlinkTimer = seconds()
        
        cursorVisible = true
    }
    
    private fun beep() = Toolkit.getDefaultToolkit().beep()
    
    private fun scroll(amount: Int) {
        scrollTarget = max(0, min(scrollTarget + amount, lineCount - linesOnScreen))
    }
    
    private fun pageUp() = scroll(-linesOnScreen)
    
    private fun pageDown() = scroll(linesOnScreen)
    
    private fun home() = scroll(-lineCount)
    
    private fun end() = scroll(lineCount)
    
    override fun keyTyped(e: KeyEvent) {
        if (inputWaiting) {
            inputBuffer.add(Glyph(e.keyChar, effect.copy(), invert))
            
            blinkCursor()
        }
    }
    
    override fun keyPressed(e: KeyEvent) {
        if (keyWaiting && keyOnPress) {
            keyQueue.put(e)
        }
        
        if (inputWaiting) {
            inputLock.withLock {
                if (e.keyCode == KeyEvent.VK_V && e.isControlDown) {
                    val text = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
                    
                    inputBuffer.addAll(text.toGlyphs(effect, invert))
                    
                    return
                }
                
                when (e.keyCode) {
                    KeyEvent.VK_ENTER     -> {
                        val line = String(input.map { it.char }.toCharArray())
                        
                        inputBuffer.clear()
                        input.clear()
                        
                        inputIndex = 0
                        inputHistoryIndex = -1
                        
                        write(line + '\n')
                        
                        inputQueue.put(line)
                    }
                    
                    KeyEvent.VK_UP        -> if (inputHistory.isNotEmpty()) {
                        end()
                        inputHistoryIndex = min(inputHistoryIndex + 1, inputHistory.lastIndex)
                        
                        inputBuffer.clear()
                        input.clear()
                        inputIndex = 0
                        
                        inputBuffer.addAll(inputHistory[inputHistoryIndex].toGlyphs(effect, invert))
                        
                        blinkCursor()
                    }
                    else {
                        beep()
                    }
                    
                    KeyEvent.VK_DOWN      -> if (inputHistory.isNotEmpty()) {
                        end()
                        inputHistoryIndex = max(inputHistoryIndex - 1, 0)
                        
                        inputBuffer.clear()
                        input.clear()
                        inputIndex = 0
                        
                        inputBuffer.addAll(inputHistory[inputHistoryIndex].toGlyphs(effect, invert))
                        
                        blinkCursor()
                    }
                    else {
                        beep()
                    }
                    
                    KeyEvent.VK_PAGE_UP   -> pageUp()
                    
                    KeyEvent.VK_PAGE_DOWN -> pageDown()
                    
                    KeyEvent.VK_HOME      -> when {
                        e.isControlDown -> if (inputWaiting) {
                            end()
                            
                            inputIndex = 0
                            blinkCursor()
                        }
                        
                        else            -> home()
                    }
                    
                    KeyEvent.VK_END       -> when {
                        e.isControlDown -> if (inputWaiting) {
                            end()
                            
                            inputIndex = inputBuffer.size
                            blinkCursor()
                        }
                        
                        else            -> end()
                    }
                    
                    KeyEvent.VK_LEFT      -> {
                        end()
                        
                        inputIndex = max(inputIndex - 1, 0)
                        
                        if (e.isControlDown) {
                            while (inputIndex > 0 && input[inputIndex - 1].char != ' ') {
                                inputIndex = max(inputIndex - 1, 0)
                            }
                        }
                        
                        blinkCursor()
                    }
                    
                    KeyEvent.VK_RIGHT     -> {
                        end()
                        
                        inputIndex = min(inputIndex + 1, input.size)
                        
                        if (e.isControlDown) {
                            while (inputIndex < input.size && input[inputIndex - 1].char != ' ') {
                                inputIndex = min(inputIndex + 1, input.size)
                            }
                        }
                        
                        blinkCursor()
                    }
                    
                    KeyEvent.VK_ESCAPE    -> {
                        end()
                        
                        inputHistoryIndex = -1
                        
                        input.clear()
                        inputBuffer.clear()
                        
                        inputIndex = 0
                        
                        blinkCursor()
                    }
                    
                    else                  -> end()
                }
            }
        }
    }
    
    override fun keyReleased(e: KeyEvent) {
        if (keyWaiting && !keyOnPress) {
            keyQueue.put(e)
        }
    }
    
    override fun mouseWheelMoved(e: MouseWheelEvent) {
        scroll(e.wheelRotation)
    }
}