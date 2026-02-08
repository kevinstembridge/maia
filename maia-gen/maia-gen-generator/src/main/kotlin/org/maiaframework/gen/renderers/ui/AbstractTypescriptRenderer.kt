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
            addImport(fieldType.idAndNameDef.pkAndNameDtoTypescriptImport)
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
        renderImportsPlaceholder()
        renderImportStatements()
        renderSourceBody()

        val newLinesForImportPlaceholder = if (this.typescriptImports.isEmpty()) "" else "\n"

        val sourceWithImportPlaceholder = sourceCode
        val renderedImportStatements = renderImportStatements().joinToString(
            prefix = newLinesForImportPlaceholder,
            separator = "\n",
        ) { it }

        return sourceWithImportPlaceholder.replace(IMPORTS_PLACEHOLDER.toRegex(), renderedImportStatements)

    }


    /**
     * We don't know what all the imports will be until after the subclass has rendered the methods in the class
     * body. So we just insert a placeholder for now and it will be replaced later.
     */
    private fun renderImportsPlaceholder() {

        appendLine(IMPORTS_PLACEHOLDER)

    }


    protected fun renderImportStatements(): List<String> {

        if (typescriptImports.isEmpty()) {
            return emptyList()
        }

        blankLine()

        return typescriptImports.groupBy { it.from }
            .toSortedMap()
            .map { (from, imports) ->

                val sortedNames = imports.map { it.name }.toSortedSet().joinToString(", ")
                "import {$sortedNames} from '$from';"

            }

    }


    abstract fun renderSourceBody()


    protected fun forEachImport(action: (TypescriptImport) -> Unit) {

        typescriptImports.sortedBy { it.name }.forEach(action)

    }


    protected fun forEachModuleImport(action: (TypescriptImport) -> Unit) {

        typescriptImports.filter { it.isModule }.sortedBy { it.name }.forEach(action)

    }


    companion object {

        private const val IMPORTS_PLACEHOLDER = "IMPORTS_PLACEHOLDER"

    }

}
