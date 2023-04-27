package kakkoiichris.kotoba.util

import org.w3c.dom.Document
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class XML(private val filePath: String) {
    lateinit var document: Document; private set
    
    fun readResource(): Boolean {
        try {
            val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            
            document = builder.parse(javaClass.getResourceAsStream(filePath))
            
            document.normalize()
        }
        catch (e: Exception) {
            e.printStackTrace()
            
            return false
        }
        
        return true
    }
    
    fun read(): Boolean {
        try {
            val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            
            document = builder.parse(FileInputStream(File(filePath)))
            
            document.normalize()
        }
        catch (e: Exception) {
            e.printStackTrace()
            
            return false
        }
        
        return true
    }
    
    fun write(): Boolean {
        try {
            val transformer = TransformerFactory.newInstance().newTransformer()
            
            val source = DOMSource(document)
            
            val result = StreamResult(File(filePath))
            
            transformer.transform(source, result)
        }
        catch (e: Exception) {
            e.printStackTrace()
            
            return false
        }
        
        return true
    }
}