package kakkoiichris.kotoba

class Glyph(internal val char: Char, private val effect: Effect, private val invert: Boolean) {
    private var color = 0
    private var offsetX = 0
    private var offsetY = 0
    
    fun update(delta: Double) {
        effect.apply(this, delta)
    }
    
    operator fun component1() = char
    
    operator fun component2() = invert
    
    operator fun component3() = color
    
    operator fun component4() = offsetX
    
    operator fun component5() = offsetY
    
    interface Effect {
        class Color(val rgb: Int) : Effect {
            companion object {
                val red = Color(0xF75DB3)
                val orange = Color(0xF9642D)
                val yellow = Color(0xFFF13A)
                val green = Color(0x6CB94B)
                val blue = Color(0x405AB9)
                val purple = Color(0xC76EDF)
                val white = Color(0xFFFFFF)
                val silver = Color(0xC4C4C4)
                val gray = Color(0x7A7A7A)
                val soot = Color(0x504D4B)
                val black = Color(0x2E2C2B)
                
                fun random() =
                    Color((Math.random() * 0xFFFFFF).toInt())
            }
            
            override fun apply(glyph: Glyph, delta: Double) {
                glyph.color = rgb
            }
            
            override fun copy() =
                this
        }
        
        class Cycle(private val speed: Double, private vararg val colors: Int) : Effect {
            private var i = 0.0
            
            override fun apply(glyph: Glyph, delta: Double) {
                glyph.color = colors[i.toInt() % colors.size]
                
                i += speed * delta
            }
            
            override fun copy() =
                Cycle(speed, *colors)
        }
        
        object Random : Effect {
            override fun apply(glyph: Glyph, delta: Double) {
                glyph.color = (Math.random() * 0xFFFFFF).toInt()
            }
            
            override fun copy() =
                this
        }
        
        class Jitter(private val x: Int, private val y: Int) : Effect {
            override fun apply(glyph: Glyph, delta: Double) {
                glyph.offsetX = (((x * 2) + 1) * Math.random()).toInt() - x
                glyph.offsetY = (((y * 2) + 1) * Math.random()).toInt() - y
            }
            
            override fun copy() =
                Jitter(x, y)
        }
        
        class Wave(
            private val amplitude: Double,
            private val frequency: Double,
            private val speed: Double,
            private val vertical: Boolean,
        ) : Effect {
            companion object {
                private var _id = 0.0
                
                val id get() = _id++
            }
            
            private var phase = 0.0
            
            override fun apply(glyph: Glyph, delta: Double) {
                if (vertical) {
                    //glyph.offsetY = amplitude * sin()
                }
                else {
                
                }
                
                phase += speed * delta
            }
            
            override fun copy() =
                Wave(amplitude, frequency, speed, vertical)
        }
        
        class Multi(vararg effects: Effect) : Effect {
            private val effects = mutableListOf<Effect>()
            
            init {
                this.effects += effects
            }
            
            override infix fun and(effect: Effect): Multi {
                if (effect is Multi) {
                    effects += effect.effects
                }
                else {
                    effects += effect
                }
                
                return this
            }
            
            override fun apply(glyph: Glyph, delta: Double) {
                effects.forEach { it.apply(glyph, delta) }
            }
            
            override fun copy() =
                Multi(*effects.map { it.copy() }.toTypedArray())
        }
        
        infix fun and(effect: Effect) =
            Multi(this, effect)
        
        fun apply(glyph: Glyph, delta: Double)
        
        fun copy(): Effect
    }
    
    data class Rule(val name: String, val regex: Regex, val effect: Effect, val invert: Boolean)
}