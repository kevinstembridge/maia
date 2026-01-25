package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.ConstructorArg


class EntityRenderer(
    entityHierarchy: EntityHierarchy
) : AbstractKotlinRenderer(
    entityHierarchy.entityDef.entityClassDef,
    entityHierarchy.hasSubclasses()
) {


    private val entityDef: EntityDef = entityHierarchy.entityDef


    private val argsForNewInstanceFunction: List<ConstructorArg>


    init {

        this.entityDef.allClassFieldsSorted.forEach { addConstructorArg(it) }
        this.argsForNewInstanceFunction = getConstructorArgs()
                .filter {
                    val classFieldName = it.classFieldDef.classFieldName
                    (
                        (classFieldName != ClassFieldName.id || entityDef.hasSurrogatePrimaryKey == false)
                        && classFieldName != ClassFieldName.createdTimestampUtc
                        && classFieldName != ClassFieldName.version
                        && classFieldName != ClassFieldName.lastModifiedById
                        && classFieldName != ClassFieldName.lastModifiedTimestampUtc
                        && classFieldName != ClassFieldName.lifecycleState
                    )
                }

    }


    override fun renderPreClassFields() {

        if (this.entityDef.hasSurrogatePrimaryKey == false && this.entityDef.isRootEntity) {

            blankLine()
            blankLine()

            if (entityDef.hasCompositePrimaryKey) {

                val primaryKeyFieldNames = fieldNamesCsv(entityDef.primaryKeyClassFields)

                appendLine("    val primaryKey = ${entityDef.entityPkClassDef.uqcn}($primaryKeyFieldNames)")

            } else {

                appendLine("    val primaryKey = ${entityDef.primaryKeyClassFields.first().classFieldName}")

            }

        }

    }

    override fun renderFunctions() {

        if (this.entityDef.isDeltaEntity.value) {
            `render function isSignificantChangeFrom`()
        }

        `render function toString`()

    }


    private fun `render function isSignificantChangeFrom`() {

        blankLine()
        blankLine()
        appendLine("    fun isSignificantChangeFrom(that: ${this.entityDef.entityClassDef.uqcn}): Boolean {")
        blankLine()
        appendLine("        return (")

        appendLines(
                this.entityDef.allDeltaFields.map { fd -> { append("                this.${fd.classFieldName} != that.${fd.classFieldName}") } },
                { appendLine(" ||") },
                {
                    newLine()
                    appendLine("        )")
                }
        )

        blankLine()
        appendLine("    }")

    }


    private fun `render function toString`() {

        blankLine()
        blankLine()
        appendLine("    override fun toString(): String {")
        blankLine()
        appendLine("        return \"${this.classDef.uqcn}{\" +")
        append("                ")

        val lines = this.entityDef.allClassFieldsSorted.map { fd ->

            val classFieldName = fd.classFieldName

            if (fd.isMasked) {
                "\"$classFieldName = 'MASKED'\" +"
            } else {
                "\"$classFieldName = '\" + this.$classFieldName + '\\'' +"
            }

        }.joinToString(" \", \" + \n                ")

        append(lines)

        appendLine("\n                \"}\"")
        blankLine()
        appendLine("    }")

    }


    override fun renderCompanionObject() {

        if (classDef.isAbstract || entityDef.isHistoryEntity) {
            return
        }

        appendLine("    companion object {")
        blankLine()
        blankLine()

        if (this.entityDef.hasSurrogatePrimaryKey) {

            appendLine("        fun newId(): DomainId {")

            when (entityDef.databaseType) {
                DatabaseType.JDBC -> appendLine("            return DomainId.newId()")
                DatabaseType.MONGO -> {
                    addImportFor(Fqcns.OBJECT_ID)
                    appendLine("            return DomainId(ObjectId.get().toHexString())")
                }
            }

            appendLine("        }")

        }

        `render function newInstance`()

        blankLine()
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()

    }


    private fun `render function newInstance`() {

        val fieldNamesNotProvidedAsArguments = mutableListOf("createdTimestampUtc")

        blankLine()
        blankLine()
        appendLine("        @JvmStatic")
        appendLine("        fun newInstance(")
        renderStrings(this.argsForNewInstanceFunction.map { "    ${it.classFieldDef.classFieldName.value}: ${it.classFieldDef.unqualifiedToString}" })
        newLine()
        appendLine("        ): ${classDef.uqcn} {")
        blankLine()
        appendLine("            val createdTimestampUtc = Instant.now()")

        if (this.entityDef.hasSurrogatePrimaryKey) {
            appendLine("            val id = newId()")
            fieldNamesNotProvidedAsArguments.add("id")
        }

        if (this.entityDef.hasLastModifiedTimestampUtcField) {

            appendLine("            val lastModifiedTimestampUtc = createdTimestampUtc")

            if (this.entityDef.hasLastModifiedByIdField) {
                appendLine("            val lastModifiedById = createdById")
                fieldNamesNotProvidedAsArguments.add("lastModifiedById")
            }

            fieldNamesNotProvidedAsArguments.add("lastModifiedTimestampUtc")

        }

        if (this.entityDef.hasLifecycleStateField) {
            appendLine("            val lifecycleState = LifecycleState.ACTIVE")
            fieldNamesNotProvidedAsArguments.add("lifecycleState")
        }

        if (this.entityDef.versioned.value) {
            appendLine("            val version = 1L")
            fieldNamesNotProvidedAsArguments.add("version")
        }

        blankLine()
        appendLine("            return ${classDef.uqcn}(")

        val fieldNames = fieldNamesNotProvidedAsArguments
                .asSequence()
                .plus(this.argsForNewInstanceFunction.map { constructorArg -> constructorArg.classFieldDef.classFieldName.value })
                .sorted()
                .toList()

        renderStrings(fieldNames, indent = 16)
        newLine()
        appendLine("            )")
        blankLine()
        appendLine("        }")

    }


}
