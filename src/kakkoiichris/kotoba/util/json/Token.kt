package kakkoiichris.kotoba.util.json

internal data class Token(val type: Type, val value: Any = Unit) {
    enum class Type {
        Value, LeftBrace, RightBrace, LeftSquare, RightSquare, Colon, Comma, EndOfFile
    }
}