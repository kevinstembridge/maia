package org.maiaframework.gen.renderers

import org.maiaframework.domain.IdAndVersion
import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.util.Optional

class EntityUpdaterRenderer(private val entityDef: EntityDef) : AbstractKotlinRenderer(entityDef.entityUpdaterClassDef) {



    init {

        val fieldUpdatesType = FieldTypes.list(FieldTypes.byFqcn(Fqcns.MAHANA_FIELD_UPDATE))

        addConstructorArg(
            aClassField("id", Fqcns.MAHANA_DOMAIN_ID) {
                constructorOnly(entityDef.databaseType == DatabaseType.MONGO)
            }.build()
        )

        addConstructorArg(
            aClassField("fields", fieldUpdatesType) {
                constructorOnly(entityDef.databaseType == DatabaseType.MONGO)
            }.build())

        if (entityDef.versioned.value) {
            addConstructorArg(
                aClassField("version", FieldTypes.long) {
                    constructorOnly(entityDef.databaseType == DatabaseType.MONGO)
                }.build())
        }

    }


    override fun renderCallToSuperConstructor(superclassDef: ClassDef) {

        if (entityDef.versioned.value) {
            appendLine("(id, version, fields)")
        } else {
            appendLine("(id, null, fields)")
        }

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

        renderForIdMethod()

    }


    private fun renderForIdMethod() {

        if (this.entityDef.versioned.value) {

            addImportFor(IdAndVersion::class.java)

            blankLine()
            blankLine()
            appendLine("        fun forIdAndVersion(id: DomainId, version: Long, init: Builder.() -> Unit): Builder {")
            blankLine()
            appendLine("            val builder = Builder(id, version)")
            appendLine("            builder.init()")
            appendLine("            return builder")
            blankLine()
            appendLine("        }")
            blankLine()
            blankLine()
            appendLine("        fun forIdAndVersion(idAndVersion: IdAndVersion, init: Builder.() -> Unit): Builder {")
            blankLine()
            appendLine("            val builder = Builder(idAndVersion.id, idAndVersion.version)")
            appendLine("            builder.init()")
            appendLine("            return builder")
            blankLine()
            appendLine("        }")

        } else {

            blankLine()
            blankLine()
            appendLine("        fun forId(id: DomainId, init: Builder.() -> Unit): Builder {")
            blankLine()
            appendLine("            val builder = Builder(id)")
            appendLine("            builder.init()")
            appendLine("            return builder")
            blankLine()
            appendLine("        }")

        }

    }


    override fun renderInnerClasses() {

        addImportFor(Fqcns.MAHANA_FIELD_UPDATE)

        blankLine()
        blankLine()

        if (entityDef.versioned.value) {
            appendLine("    class Builder(val id: DomainId, val version: Long) {")
        } else {
            appendLine("    class Builder(val id: DomainId) {")
        }

        blankLine()
        blankLine()
        appendLine("        private val fields = mutableListOf<FieldUpdate>()")
        blankLine()
        blankLine()
        appendLine("        fun build(): ${classDef.uqcn} {")
        blankLine()

        if (entityDef.versioned.value) {
            appendLine("            return ${classDef.uqcn}(this.fields, this.id, this.version)")
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
