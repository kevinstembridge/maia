package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.IdAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


abstract class AbstractTypescriptRenderer: AbstractSourceFileRenderer() {


    private val typescriptImports = mutableListOf<TypescriptImport>()


    fun addImport(
        from: String,
        name: String,
        isModule: Boolean = false
    ) {

        addImport(TypescriptImport(name, from, isModule))

    }


    fun addImport(import: TypescriptImport) {

        this.typescriptImports.add(import)

    }


    protected fun addImportsFor(
        fieldType: FieldType
    ) {

        if (fieldType is EnumFieldType) {
            addImport(fieldType.enumDef.typescriptImport)
        }

        if (fieldType is SimpleResponseDtoFieldType) {
            addImport(fieldType.responseDtoDef.dtoDef.typescriptDtoImport)
        }

        if (fieldType is RequestDtoFieldType) {
            addImport(fieldType.requestDtoDef.typescriptImport)
        }

        if (fieldType is EsDocFieldType) {
            addImport(fieldType.esDocDef.dtoDef.typescriptDtoImport)
        }

        if (fieldType is IdAndNameFieldType) {
            addImport(fieldType.idAndNameDef.idAndNameDtoTypescriptImport)
        }

        if (fieldType is MapFieldType) {
            addImportsFor(fieldType.keyFieldType)
            addImportsFor(fieldType.valueFieldType)
        }

        if (fieldType is ListFieldType) {
            addImportsFor(fieldType.parameterFieldType)
        }

    }


    override fun renderSource(): String {

        renderGeneratedCodeStatement()
        renderImportStatements()
        renderSourceBody()
        return sourceCode.toString()

    }


    protected fun renderImportStatements() {

        if (typescriptImports.isEmpty()) {
            return
        }

        blankLine()

        typescriptImports.groupBy { it.from }
            .toSortedMap()
            .forEach { (from, imports) ->

                val sortedNames = imports.map { it.name }.toSortedSet().joinToString(", ")
                appendLine("import {$sortedNames} from '$from';")

            }

    }


    abstract fun renderSourceBody()


    protected fun forEachImport(action: (TypescriptImport) -> Unit) {

        typescriptImports.sortedBy { it.name }.forEach(action)

    }


    protected fun forEachModuleImport(action: (TypescriptImport) -> Unit) {

        typescriptImports.filter { it.isModule }.sortedBy { it.name }.forEach(action)

    }


}
