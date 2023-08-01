package com.typeboot.dataformat.factory

import com.fasterxml.jackson.databind.node.ObjectNode
import com.opencsv.CSVParser
import com.typeboot.dataformat.config.YamlSupport
import com.typeboot.dataformat.generator.Generator
import com.typeboot.dataformat.generator.GeneratorFactory
import com.typeboot.dataformat.renderer.Renderer
import com.typeboot.dataformat.scripts.DefaultScriptNumberProvider
import com.typeboot.dataformat.scripts.FileScript
import com.typeboot.dataformat.types.*
import java.io.File
import java.io.StringReader
import java.util.regex.Pattern
import javax.script.Invocable
import javax.script.ScriptEngineManager

class TypeFactory(name: String = ".typeboot.yaml") {
    private val yaml = YamlSupport()
    private val spec = yaml.toInstance(name, SpecConfig::class.java)

    fun generate() {
        val generators = spec.getGenerators()

        generators.forEach { name ->
            val generator = GeneratorFactory.render(name, spec.provider)
            val serialisation = generator.serialisationProps()
            val renderer = Renderer.create(spec.output, serialisation)
            generateDml(generator, renderer)
            generateData(renderer)
        }
    }

    private fun generateDml(generator: Generator, renderer: Renderer) {
        spec.getSources().forEach { fileScript ->
            val fileName = fileScript.filePath
            val tables = yaml.toList(fileName)
            val ins = tables.flatMap<ObjectNode, Instructions> { table ->
                val tableDef = table.toPrettyString()
                val data = tableDef.toByteArray()
                when (table.get("kind").textValue()) {
                    "SchemaDefinition" -> generator.generateSchema(yaml.toInstance(data, SchemaDefinition::class.java))
                    "TableDefinition" -> generator.generateTable(yaml.toInstance(data, TableDefinition::class.java))
                    "ColumnDefinition" -> generator.generateColumn(yaml.toInstance(data, ColumnDefinition::class.java))
                    "ColumnRemovalDefinition" -> generator.generateColumnRemoval(yaml.toInstance(data, ColumnRemovalDefinition::class.java))
                    "TableRemovalDefinition" -> generator.generateTableRemoval(yaml.toInstance(data, TableRemovalDefinition::class.java))
                    "ColumnRenameDefinition" -> generator.generateColumnRename(yaml.toInstance(data, ColumnRenameDefinition::class.java))
                    else -> listOf()
                }
            }
            renderer.render(fileScript, ins)
        }
    }

    private fun generateData(renderer: Renderer) {
        val csvParser = CSVParser()
        val engine = ScriptEngineManager(javaClass.classLoader).getEngineByExtension("kts")!!
        val mapFunctionInvoker = engine as? Invocable
        spec.getTemplates().forEach { fileScript ->
            val parent = fileScript.getParent()
            val fileName = fileScript.filePath
            val dataFiles = yaml.toList(fileName)
            val dataDefinitionList = dataFiles.flatMap { dataFile ->
                val dataDef = dataFile.toPrettyString()
                val data = dataDef.toByteArray()
                when (dataFile.get("kind").textValue()) {
                    "DataTemplateDefinition" -> listOf(yaml.toInstance(data, DataTemplateDefinition::class.java))
                    else -> listOf()
                }
            }
            dataDefinitionList.forEach { d ->
                val headerNames = d.headers.map { h -> h.name }
                val scriptProvider = DefaultScriptNumberProvider(Pattern.compile("^[V]?([0-9]+)(.*)\\.csv$"))
                engine.eval(StringReader(d.generator))
                d.resources.forEach { resource ->
                    val dataFilePath = "${parent}/../data/${resource}"
                    val scriptName = scriptProvider.scriptForName(resource)
                    val dataFileScript = FileScript(scriptName, dataFilePath)
                    renderer.preRender(dataFileScript)
                    File(dataFilePath).forEachLine { line ->
                        val dataMap = headerNames.zip(csvParser.parseLine(line)).toMap()
                        val mapResult = mapFunctionInvoker!!.invokeFunction("map", mapOf("schema" to d.subject.schema,
                                "table" to d.subject.table!!,
                                "data" to dataMap
                        ))
                        renderer.renderInstructions(Instructions(mapResult.toString()))
                    }
                    renderer.postRender(fileScript)
                }
            }
        }
    }
}