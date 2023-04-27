package kakkoiichris.kotoba.util

import kakkoiichris.kotoba.Font
import kakkoiichris.kotoba.QuickScript
import kakkoiichris.kotoba.util.json.JSON
import java.io.File

/**
 * Kotoba-Kotlin
 
 * Copyright (C) 2022, KakkoiiChris
 *
 * File:    Resources.kt
 
 * Created: Thursday, September 08, 2022, 21:25:11
 *
 * @author Christian Bryce Alexander
 */
class Resources(rootPath: String) {
    private val root = Folder(rootPath)
    
    private var current = root
    
    fun getFont(name: String) =
        current.getFont(name)
    
    fun getFontOrNull(name: String) =
        current.getFontOrNull(name)
    
    fun getCSV(name: String) =
        current.getCSV(name)
    
    fun getCSVOrNull(name: String) =
        current.getCSVOrNull(name)
    
    fun getJSON(name: String) =
        current.getJSON(name)
    
    fun getJSONOrNull(name: String) =
        current.getJSONOrNull(name)
    
    fun getQuickScript(name: String) =
        current.getQuickScript(name)
    
    fun getQuickScriptOrNull(name: String) =
        current.getQuickScriptOrNull(name)
    
    fun getTXT(name: String) =
        current.getTXT(name)
    
    fun getTXTOrNull(name: String) =
        current.getTXTOrNull(name)
    
    fun getXML(name: String) =
        current.getXML(name)
    
    fun getXMLOrNull(name: String) =
        current.getXMLOrNull(name)
    
    fun getFolder(name: String) =
        current.getFolder(name)
    
    fun getFolderOrNull(name: String) =
        current.getFolderOrNull(name)
    
    fun goBack(): Boolean {
        current = current.parent ?: return false
        
        return true
    }
    
    fun goTo(name: String): Boolean {
        current = current.getFolderOrNull(name) ?: return false
        
        return true
    }
    
    class Folder(private val path: String, val parent: Folder? = null) {
        private val fonts = mutableMapOf<String, Font>()
        private val csvFiles = mutableMapOf<String, CSV>()
        private val jsonFiles = mutableMapOf<String, JSON>()
        private val kqFiles = mutableMapOf<String, QuickScript>()
        private val txtFiles = mutableMapOf<String, TXT>()
        private val xmlFiles = mutableMapOf<String, XML>()
        private val subFolders = mutableMapOf<String, Folder>()
        
        init {
            val resource = javaClass.getResource(path) ?: error("Unable to load resource '$path'!")
            
            val file = File(resource.toURI())
            
            val files = file.listFiles()?.toList() ?: emptyList()
            
            files.filter { it.isFile }.forEach {
                val resourceName = it.nameWithoutExtension
                val resourcePath = "$path/${it.name}"
                
                when (it.extension.lowercase()) {
                    "bff"  -> fonts[resourceName] = Font(resourcePath)
                    
                    "csv"  -> csvFiles[resourceName] = CSV(resourcePath).apply { readResource() }
                    
                    "json" -> jsonFiles[resourceName] = JSON(resourcePath).apply { readResource() }
                    
                    "kq"   -> kqFiles[resourceName] = QuickScript(TXT(resourcePath).apply { readResource() }.lines)
                    
                    "txt"  -> txtFiles[resourceName] = TXT(resourcePath).apply { readResource() }
                    
                    "xml"  -> xmlFiles[resourceName] = XML(resourcePath).apply { readResource() }
                }
            }
            
            files.filter { it.isDirectory }.forEach {
                subFolders[it.name] = Folder("$path/${it.name}", this)
            }
        }
        
        fun getFont(name: String) =
            fonts[name] ?: error("Font '$path/$name' does not exist!")
        
        fun getFontOrNull(name: String) =
            fonts[name]
        
        fun getCSV(name: String) =
            csvFiles[name] ?: error("CSV file '$path/$name' does not exist!")
        
        fun getCSVOrNull(name: String) =
            csvFiles[name]
        
        fun getJSON(name: String) =
            jsonFiles[name] ?: error("JSON file '$path/$name' does not exist!")
        
        fun getJSONOrNull(name: String) =
            jsonFiles[name]
    
        fun getQuickScript(name: String) =
            kqFiles[name] ?: error("QuickScript file '$path/$name' does not exist!")
    
        fun getQuickScriptOrNull(name: String) =
            kqFiles[name]
        
        fun getTXT(name: String) =
            txtFiles[name] ?: error("Text file '$path/$name' does not exist!")
        
        fun getTXTOrNull(name: String) =
            txtFiles[name]
        
        fun getXML(name: String) =
            xmlFiles[name] ?: error("XML file '$path/$name' does not exist!")
        
        fun getXMLOrNull(name: String) =
            xmlFiles[name]
        
        fun getFolder(name: String) =
            subFolders[name] ?: error("Folder '$path/$name' does not exist!")
        
        fun getFolderOrNull(name: String) =
            subFolders[name]
    }
}