package kakkoiichris.kotoba.util.json

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

sealed class Node(var value: Any) {
    fun asValue() = (this as? Value) ?: error("Node is not a value!")
    
    fun asBoolean() = (asValue().value as? Boolean) ?: error("Node is not a boolean value!")
    
    fun asByte() = ((asValue().value as? Double) ?: error("Node is not a byte value!")).toInt().toByte()
    
    fun asShort() = ((asValue().value as? Double) ?: error("Node is not a short value!")).toInt().toShort()
    
    fun asInt() = ((asValue().value as? Double) ?: error("Node is not an int value!")).toInt()
    
    fun asLong() = ((asValue().value as? Double) ?: error("Node is not a long value!")).toLong()
    
    fun asFloat() = ((asValue().value as? Double) ?: error("Node is not a float value!")).toFloat()
    
    fun asDouble() = (asValue().value as? Double) ?: error("Node is not a double value!")
    
    fun asChar() = (asValue().value as? Char) ?: error("Node is not a char value!")
    
    fun asString() = (asValue().value as? String) ?: error("Node is not a string value!")
    
    fun asArray() = this as? Array ?: error("Node is not an array!")
    
    fun asBooleanArray() = asArray().map {
        (it.asValue().value as? Boolean) ?: error("Node is not a boolean array!")
    }.toBooleanArray()
    
    fun asByteArray() = asArray().map {
        ((it.asValue().value as? Double) ?: error("Node is not a byte array!")).toInt().toByte()
    }.toByteArray()
    
    fun asShortArray() = asArray().map {
        ((it.asValue().value as? Double) ?: error("Node is not a short array!")).toInt().toShort()
    }.toShortArray()
    
    fun asIntArray() = asArray().map {
        ((it.asValue().value as? Double) ?: error("Node is not an int array!")).toInt()
    }.toIntArray()
    
    fun asLongArray() = asArray().map {
        ((it.asValue().value as? Double) ?: error("Node is not a long array!")).toLong()
    }.toLongArray()
    
    fun asFloatArray() = asArray().map {
        ((it.asValue().value as? Double) ?: error("Node is not a float array!")).toFloat()
    }.toFloatArray()
    
    fun asDoubleArray() = asArray().map {
        (it.asValue().value as? Double) ?: error("Node is not a double array!")
    }.toDoubleArray()
    
    fun asCharArray() = asArray().map {
        (it.asValue().value as? Char) ?: error("Node is not a char array!")
    }.toCharArray()
    
    fun asStringArray() = asArray().map {
        (it.asValue().value as? String) ?: error("Node is not a string array!")
    }.toTypedArray()
    
    fun asArrayArray() = asArray().map {
        it.asArray()
    }.toTypedArray()
    
    fun asObjectArray() = asArray().map {
        it.asObject()
    }.toTypedArray()
    
    fun asObject() = this as? Object ?: error("Node is not an object!")
    
    abstract fun format(indent: Int): String
    
    override fun toString() =
        format(0)
    
    class Value(value: Any) : Node(value) {
        override fun format(indent: Int) = when (value) {
            is String -> "\"$value\""
            
            else      -> "$value"
        }
    }
    
    class Array(list: MutableList<Node> = mutableListOf()) : Node(list), MutableList<Node> by list {
        fun addValue(value: Any) = add(Value(value))
        
        fun addValue(index: Int, value: Any) = add(index, Value(value))
        
        fun addArray() = add(Array())
        
        fun addArray(index: Int) = add(index, Array())
        
        fun addObject() = add(Object())
        
        fun addObject(index: Int) = add(index, Object())
        
        override fun format(indent: Int) = buildString {
            val whitespace = " ".repeat(indent)
            
            append("$whitespace[\n")
            
            var first = true
            
            this@Array.forEach { node ->
                if (!first) {
                    append(",\n")
                }
                
                append("$whitespace${node.format(indent + 1)}")
                
                first = false
            }
            
            append("\n$whitespace]")
        }
    }
    
    class Object(map: MutableMap<String, Node> = mutableMapOf()) : Node(map), MutableMap<String, Node> by map {
        fun addValue(name: String, value: Any) = put(name, Value(value))
        
        fun addArray(name: String) = put(name, Array())
        
        fun addObject(name: String) = put(name, Object())
        
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> create(clazz: KClass<T>): T? {
            val parameters = clazz.primaryConstructor?.parameters ?: error("No Constructor Parameters!")
            
            val arguments = parameters.associateWith { kParameter ->
                val annotation =
                    kParameter.annotations.firstOrNull { it is JSONMember } as? JSONMember
                
                val name = annotation?.name?.takeIf { it.isNotEmpty() } ?: kParameter.name
                
                val value = this[name] ?: error("Cannot find value at '${name}'!")
                
                val type = kParameter.type.classifier as? KClass<T> ?: error("")
                
                val realValue: Any? = when (value) {
                    is Value  -> when (type) {
                        Boolean::class -> value.asBoolean()
                        Byte::class    -> value.asByte()
                        Short::class   -> value.asShort()
                        Int::class     -> value.asInt()
                        Long::class    -> value.asLong()
                        Float::class   -> value.asFloat()
                        Double::class  -> value.asDouble()
                        Char::class    -> value.asChar()
                        String::class  -> value.asString()
                        else           -> null
                    }
                    
                    is Array  -> when (type) {
                        BooleanArray::class         -> value.asBooleanArray()
                        ByteArray::class            -> value.asByteArray()
                        ShortArray::class           -> value.asShortArray()
                        IntArray::class             -> value.asIntArray()
                        LongArray::class            -> value.asLongArray()
                        FloatArray::class           -> value.asFloatArray()
                        DoubleArray::class          -> value.asDoubleArray()
                        CharArray::class            -> value.asCharArray()
                        kotlin.Array<String>::class -> value.asStringArray()
                        else                        -> null
                    }
                    
                    is Object -> {
                        val subClazz = Class.forName(annotation?.classType ?: "")
                        
                        value.create(subClazz.kotlin)
                    }
                }
                
                realValue
            }
            
            return clazz.primaryConstructor?.callBy(arguments)
        }
        
        override fun format(indent: Int) = buildString {
            val whitespace = " ".repeat(indent)
            
            append("$whitespace{\n")
            
            var first = true
            
            this@Object.forEach { key, value ->
                if (!first) {
                    append(",\n")
                }
                
                append("$whitespace\"$key\" : ${value.format(indent + 1)}")
                
                first = false
            }
            
            append("\n$whitespace}")
        }
    }
}

