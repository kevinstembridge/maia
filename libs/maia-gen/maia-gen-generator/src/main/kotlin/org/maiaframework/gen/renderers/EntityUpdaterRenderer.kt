package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ConstructorArg
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.util.Optional

class EntityUpdaterRenderer(private val entityDef: EntityDef) : AbstractKotlinRenderer(entityDef.entityUpdaterClassDef) {


    init {

        val fieldUpdatesType = FieldTypes.list(FieldTypes.byFqcn(Fqcns.MAIA_FIELD_UPDATE))

        val constructorFields = mutableListOf<ClassFieldDef>()

        val fieldsClassFieldDef = aClassField("fields", fieldUpdatesType) {
            constructorOnly(entityDef.databaseType == DatabaseType.MONGO)
        }.build()

        constructorFields.add(fieldsClassFieldDef)

        val primaryKeyClassFieldDefs = this.entityDef.primaryKeyFields.map { fieldDef ->
            aClassField(fieldDef.classFieldName, fieldDef.fieldType) {
                constructorOnly(entityDef.databaseType == DatabaseType.MONGO)
            }.build()
        }

        constructorFields.addAll(primaryKeyClassFieldDefs)

        if (entityDef.versioned.value) {

            val versionClassFieldDef = aClassField("version", FieldTypes.long) {
                constructorOnly(entityDef.databaseType == DatabaseType.MONGO)
            }.build()

            constructorFields.add(versionClassFieldDef)

        }

        setConstructorArgs(constructorFields.map { ConstructorArg(it) })

    }


    override fun renderCallToSuperConstructor(superclassDef: ClassDef) {

        if (entityDef.versioned.value) {
            appendLine("(id, version, fields)")
        } else {
            appendLine("(id, null, fields)")
        }

    }


    override fun renderPreClassFields() {

        blankLine()
        blankLine()
        appendLine("    val primaryKey = mapOf(")

        this.entityDef.primaryKeyFields.forEach { fieldDef ->
            appendLine("        \"${fieldDef.classFieldName}\" to ${fieldDef.classFieldName},")
        }

        appendLine("    )")

    }

    override fun renderFunctions() {

        // TODO Only render for Mongo entities
//        renderToBsonMethod()


    }


    private fun renderToBsonMethod() {

        addImportFor(Fqcns.BSON)
        addImportFor(Fqcns.BSON_DOCUMENT)
        addImportFor(Optional::class.java)

        blankLine()
        blankLine()
        appendLine("    fun toBson(fieldConverter: ${this.entityDef.entityFieldConverterClassDef.uqcn}): Bson {")
        blankLine()
        appendLine("        val setFieldsDocument = Document()")
        appendLine("        val unsetFieldsDocument = Document()")
        blankLine()
        appendLine("        this.fields.forEach { fieldUpdate -> ")
        blankLine()
        appendLine("            val fieldValue = fieldUpdate.value")
        appendLine("            val collectionFieldName = fieldUpdate.dbColumnName")
        blankLine()
        appendLine("            if (fieldValue == null) {")
        blankLine()
        appendLine("                unsetFieldsDocument.put(collectionFieldName, \"\")")
        blankLine()
        appendLine("            } else if (fieldValue is Optional<*>) {")
        blankLine()
        appendLine("                if (fieldValue.isPresent) {")
        appendLine("                    setFieldsDocument.put(collectionFieldName, fieldConverter.convert(collectionFieldName, fieldValue.get()))")
        appendLine("                } else {")
        appendLine("                    unsetFieldsDocument.put(collectionFieldName, \"\")")
        appendLine("                }")
        blankLine()
        appendLine("            } else {")
        blankLine()
        appendLine("                setFieldsDocument.put(collectionFieldName, fieldConverter.convert(collectionFieldName, fieldValue))")
        blankLine()
        appendLine("            }")
        blankLine()
        appendLine("        }")
        blankLine()
        appendLine("        val document = Document()")
        blankLine()
        appendLine("        if (setFieldsDocument.isNotEmpty()) {")
        appendLine("            document.put(\"\\\$set\", setFieldsDocument)")
        appendLine("        }")
        blankLine()
        appendLine("        if (unsetFieldsDocument.isNotEmpty()) {")
        appendLine("            document.put(\"\\\$unset\", unsetFieldsDocument)")
        appendLine("        }")
        blankLine()
        appendLine("        return document")
        blankLine()
        appendLine("    }")

    }


    override fun renderCompanionObject() {

        appendLine("    companion object {")
        renderCompanionObjectBody()
        blankLine()
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()

    }


    private fun renderCompanionObjectBody() {

        renderCompanionObjectFunctions()

    }


    private fun renderCompanionObjectFunctions() {

        `render function forPrimaryKey`()

    }


    private fun `render function forPrimaryKey`() {

        val fieldNamesAndTypesCsv = fieldNamesAndTypesCsv(this.entityDef.primaryKeyClassFields)
        val fieldNamesCsv = fieldNamesCsv(this.entityDef.primaryKeyClassFields)

        if (this.entityDef.versioned.value) {

            blankLine()
            blankLine()
            appendLine("        fun forPrimaryKey(")

            this.entityDef.primaryKeyClassFields.forEach { fieldDef ->
                appendLine("            ${fieldDef.classFieldName}: ${fieldDef.fieldType.unqualifiedToString},")
            }

            appendLine("            version: Long,")
            appendLine("            init: Builder.() -> Unit")
            appendLine("        ): Builder {")
            blankLine()
            appendLine("            val builder = Builder(")

            this.entityDef.primaryKeyClassFields.forEach { fieldDef ->
                appendLine("                ${fieldDef.classFieldName},")
            }

            appendLine("                version")
            appendLine("            )")
            appendLine("            builder.init()")
            appendLine("            return builder")
            blankLine()
            appendLine("        }")

        } else {

            blankLine()
            blankLine()
            appendLine("        fun forPrimaryKey(")

            this.entityDef.primaryKeyClassFields.forEach { fieldDef ->
                appendLine("            ${fieldDef.classFieldName}: ${fieldDef.fieldType.unqualifiedToString},")
            }

            appendLine("            init: Builder.() -> Unit): Builder {")
            appendLine("        ): Builder {")
            blankLine()
            appendLine("            val builder = Builder(id)")
            appendLine("            builder.init()")
            appendLine("            return builder")
            blankLine()
            appendLine("        }")

        }

    }


    override fun renderInnerClasses() {

        addImportFor(Fqcns.MAIA_FIELD_UPDATE)

        val fieldNamesAndTypesCsv = this.entityDef.primaryKeyClassFields.joinToString(", ") { fieldDef ->
            "val ${fieldDef.classFieldName}: ${fieldDef.fieldType.unqualifiedToString}"
        }

        blankLine()
        blankLine()

        if (entityDef.versioned.value) {
            appendLine("    class Builder(")

            this.entityDef.primaryKeyClassFields.forEach { fieldDef ->
                appendLine("        val ${fieldDef.classFieldName}: ${fieldDef.fieldType.unqualifiedToString},")
            }

            appendLine("        val version: Long")
            appendLine("    ) {")
        } else {
            appendLine("    class Builder($fieldNamesAndTypesCsv) {")
        }

        blankLine()
        blankLine()
        appendLine("        private val fields = mutableListOf<FieldUpdate>()")
        blankLine()
        blankLine()
        appendLine("        fun build(): ${classDef.uqcn} {")
        blankLine()

        if (entityDef.versioned.value) {

            appendLine("            return ${classDef.uqcn}(")
            appendLine("                this.fields,")

            this.entityDef.primaryKeyClassFields.forEach { fieldDef ->
                appendLine("                this.${fieldDef.classFieldName},")
            }

            appendLine("                this.version")
            appendLine("            )")

        } else {
            appendLine("            return ${classDef.uqcn}(this.fields, this.id)")
        }

        blankLine()
        appendLine("        }")

        this.entityDef.allFieldsForEntityUpdaters.forEach { fieldDef ->

            val classFieldDef = fieldDef.classFieldDef
            val classFieldName = classFieldDef.classFieldName
            val tableColumnName = fieldDef.dbColumnFieldDef.tableColumnName

            addImportFor(classFieldDef.fieldType)

            blankLine()
            blankLine()
            appendLine("        fun $classFieldName($classFieldName: ${classFieldDef.unqualifiedToString}) {")
            blankLine()
            appendLine("            this.fields.add(FieldUpdate(\"$classFieldName\", \"$tableColumnName\", $classFieldName))")
            blankLine()
            appendLine("        }")

        }

        blankLine()
        blankLine()
        appendLine("    }")

    }


}
