package kakkoiichris.kotoba.util

import kakkoiichris.kotoba.util.json.JSON
import kakkoiichris.kotoba.Font
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
    
    private var cd = root
    
    fun getFont(name: String) =
        cd.getFont(name)
    
    fun getFontOrNull(name: String) =
        cd.getFontOrNull(name)
    
    fun getCSV(name: String) =
        cd.getCSV(name)
    
    fun getCSVOrNull(name: String) =
        cd.getCSVOrNull(name)
    
    fun getJSON(name: String) =
        cd.getJSON(name)
    
    fun getJSONOrNull(name: String) =
        cd.getJSONOrNull(name)
    
    fun getTXT(name: String) =
        cd.getTXT(name)
    
    fun getTXTOrNull(name: String) =
        cd.getTXTOrNull(name)
    
    fun getXML(name: String) =
        cd.getXML(name)
    
    fun getXMLOrNull(name: String) =
        cd.getXMLOrNull(name)
    
    fun getFolder(name: String) =
        cd.getFolder(name)
    
    fun getFolderOrNull(name: String) =
        cd.getFolderOrNull(name)
    
    fun goBack(): Boolean {
        cd = cd.parent ?: return false
        
        return true
    }
    
    fun goTo(name: String): Boolean {
        cd = cd.getFolderOrNull(name) ?: return false
        
        return true
    }
    
    class Folder(private val path: String, val parent: Folder? = null) {
        private val fonts = mutableMapOf<String, Font>()
        private val csvFiles = mutableMapOf<String, CSV>()
        private val jsonFiles = mutableMapOf<String, JSON>()
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