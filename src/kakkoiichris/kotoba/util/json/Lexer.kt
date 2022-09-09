package kakkoiichris.kotoba.util.json

import kakkoiichris.kotoba.util.json.Token.Type.*

internal class Lexer(private val source: String) {
    companion object {
        private const val NUL = '\u0000'
    }
    
    private var pos = 0
    
    fun next(): Token {
        while (!match(NUL)) {
            if (match { isWhitespace() }) {
                skipWhitespace()
            }
            
            return when {
                match { isLetter() }              -> literal()
                
                match('-') || match { isDigit() } -> number()
                
                match { this in "'\"" }           -> string()
                
                else                              -> symbol()
            }
        }
        
        return Token(EndOfFile)
    }
    
    private fun peek() =
        if (pos in source.indices)
            source[pos]
        else
            NUL
    
    private fun step() {
        pos++
    }
    
    private fun match(char: Char) =
        peek() == char
    
    private fun match(predicate: Char.() -> Boolean) =
        peek().predicate()
    
    private fun skip(char: Char): Boolean {
        if (match(char)) {
            step()
            
            return true
        }
        
        return false
    }
    
    private fun skip(predicate: Char.() -> Boolean): Boolean {
        if (match(predicate)) {
            step()
            
            return true
        }
        
        return false
    }
    
    private fun mustSkip(char: Char) {
        if (!skip(char)) {
            error("Expected character '$char'; encountered character '${peek()}'!")
        }
    }
    
    private fun isEOF() =
        match(NUL)
    
    private fun skipWhitespace() {
        while (!isEOF() && skip { isWhitespace() }) Unit
    }
    
    private fun StringBuilder.take() {
        append(peek())
        
        step()
    }
    
    private fun number(): Token {
        val result = buildString {
            do {
                take()
            }
            while (!isEOF() && match { isDigit() })
            
            if (match('.')) {
                do {
                    take()
                }
                while (!isEOF() && match { isDigit() })
            }
        }
        
        return Token(Value, result.toDouble())
    }
    
    private fun string(): Token {
        val delimiter = peek()
        
        mustSkip(delimiter)
        
        val result = buildString {
            while (!isEOF() && !skip(delimiter)) {
                if (skip('\\')) {
                    append(when {
                        skip('\\')      -> '\\'
                        
                        skip(delimiter) -> delimiter
                        
                        skip('b')       -> '\b'
                        
                        skip('f')       -> '\u000c'
                        
                        skip('n')       -> '\n'
                        
                        skip('r')       -> '\r'
                        
                        skip('t')       -> '\t'
                        
                        skip('u')       -> unicode()
                        
                        else            -> error("Invalid character escape '\\${peek()}'!")
                    })
                }
                else {
                    take()
                }
            }
        }
        
        return Token(Value, result)
    }
    
    private fun unicode() =
        buildString(4) { repeat(4) { take() } }
            .toInt(16)
            .toChar()
    
    private fun literal(): Token {
        val result = buildString {
            do {
                take()
            }
            while (!isEOF() && match { isLetter() })
        }
        
        return Token(Value, when (result) {
            "true"  -> true
            
            "false" -> false
            
            else    -> error("Invalid literal '$result'!")
        })
    }
    
    private fun symbol() =
        Token(when {
            skip('{') -> LeftBrace
            
            skip('}') -> RightBrace
            
            skip('[') -> LeftSquare
            
            skip(']') -> RightSquare
            
            skip(':') -> Colon
            
            skip(',') -> Comma
            
            else      -> error("Invalid character '${peek()}'!")
        })
}
