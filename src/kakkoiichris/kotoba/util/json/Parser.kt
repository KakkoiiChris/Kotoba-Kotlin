package kakkoiichris.kotoba.util.json

import kakkoiichris.kotoba.util.json.Token.Type
import kakkoiichris.kotoba.util.json.Token.Type.*

internal class Parser(private val lexer: Lexer) {
    private var currentToken = lexer.next()
    
    fun parse() =
        parseObject()
    
    private fun step() {
        currentToken = lexer.next()
    }
    
    private fun match(type: Type) =
        currentToken.type == type
    
    private fun mustSkip(vararg types: Type) {
        for (type in types) {
            if (match(type)) {
                step()
            }
            else {
                error("Invalid token type '${currentToken.type}'!")
            }
        }
    }
    
    private fun parseObject(): Node.Object {
        mustSkip(LeftBrace)
        
        val members = mutableMapOf<String, Node>()
        
        var first = true
        
        do {
            if (!first) {
                mustSkip(Comma)
            }
            
            val name =
                currentToken.value as? String ?: error("JSON member name '${currentToken.value}' must be a string!")
            
            mustSkip(Value, Colon)
            
            val node = when {
                match(LeftBrace)  -> parseObject()
                
                match(LeftSquare) -> parseArray()
                
                else              -> parseValue()
            }
            
            members[name] = node
            
            first = false
        }
        while (match(Comma))
        
        mustSkip(RightBrace)
        
        return Node.Object(members)
    }
    
    private fun parseArray(): Node.Array {
        mustSkip(LeftSquare)
        
        val elements = mutableListOf<Node>()
        
        var first = true
        
        do {
            if (!first) {
                mustSkip(Comma)
            }
    
            val node = when {
                match(LeftBrace)  -> parseObject()
        
                match(LeftSquare) -> parseArray()
        
                else              -> parseValue()
            }
            
            elements.add(node)
            
            first = false
        }
        while (match(Comma))
        
        mustSkip(RightSquare)
        
        return Node.Array(elements)
    }
    
    private fun parseValue(): Node.Value {
        val value = currentToken.value
        
        mustSkip(Value)
        
        return Node.Value(value)
    }
}
