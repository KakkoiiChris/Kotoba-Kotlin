package kakkoiichris.kotoba.util.json

import java.io.*

class JSON(private val filePath: String) {
    lateinit var root: Node.Object; private set
    
    private var isResource = false
    
    fun readResource(): Boolean {
        val source = javaClass
            .getResourceAsStream(filePath)
            ?.bufferedReader()
            ?.readText()
            ?: return false
        
        val lexer = Lexer(source)
        val parser = Parser(lexer)
        root = parser.parse()
        
        isResource = true
        
        return true
    }
    
    fun read(): Boolean {
        if (isResource) return false
        
        val source = BufferedReader(InputStreamReader(FileInputStream(filePath))).readText()
        
        val lexer = Lexer(source)
        val parser = Parser(lexer)
        root = parser.parse()
        
        return true
    }
    
    fun write(): Boolean {
        if (isResource) return false
        
        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(filePath), "utf-8"))
        
        //for (row in rows) {
        //    writer.write(row.toString())
        //    writer.newLine()
        //}
        
        writer.close()
        
        return true
    }
    
    operator fun get(name: String) =
        root[name]
}