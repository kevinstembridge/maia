package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityFieldRowMapperFieldDef
import org.maiaframework.gen.spec.definition.ForeignKeyRowMapperFieldDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.ManyToManyRowMapperFieldDef
import org.maiaframework.gen.spec.definition.RowMapperDef
import org.maiaframework.gen.spec.definition.RowMapperFunctions.renderRowMapperField
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class RowMapperRenderer(
    private val rowMapperDef: RowMapperDef
): AbstractKotlinRenderer(
    rowMapperDef.classDef
) {


    init {

        if (rowMapperDef.entityFieldDefs.any { it.entityFieldDef.classFieldDef.isMap == true }) {
            addConstructorArg(ClassFieldDef.aClassField("jsonMapper", Fqcns.JACKSON_JSON_MAPPER).privat().build())
        }

        if (rowMapperDef.manyToManyFieldDefs.isNotEmpty()) {
            addConstructorArg(ClassFieldDef.aClassField("jdbcOps", Fqcns.MAIA_JDBC_OPS).privat().build())
        }

    }


    override fun renderPreClassFields() {

        this.rowMapperDef.manyToManyFieldDefs.forEach { manyToManyRowMapperFieldDef ->

            val entityPkAndNameDef = manyToManyRowMapperFieldDef.entityPkAndNameDef

            append("""
                |
                |
                |    private val ${manyToManyRowMapperFieldDef.classFieldName}PkAndNameDtoRowMapper = ${entityPkAndNameDef.rowMapperDef.classDef.uqcn}()
                |""".trimMargin())

        }

    }


    override fun renderFunctions() {

        `render function mapRow`()
        `render functions for manyToManyPkAndNameDtos`()

    }


    private fun `render function mapRow`() {

        addImportFor(Fqcns.MAIA_RESULT_SET_ADAPTER)

        append("""
            |
            |
            |    override fun mapRow(rsa: ResultSetAdapter): ${rowMapperDef.rowUqcn} {
            |
            |""".trimMargin()
        )

        if (rowMapperDef.manyToManyFieldDefs.isNotEmpty()) {

            appendLine("        val entityId = rsa.readDomainId(\"id\")")
            blankLine()

            rowMapperDef.manyToManyFieldDefs.forEach { manyToManyRowMapperFieldDef ->

                val classFieldName = manyToManyRowMapperFieldDef.classFieldName
                appendLine("        val ${classFieldName}PkAndNameDtoList = fetch${manyToManyRowMapperFieldDef.classFieldName.firstToUpper()}PkAndNameDtos(entityId)")
                blankLine()
            }

        }

        appendLine("        return ${rowMapperDef.rowUqcn}(")

        rowMapperDef.fieldDefs.forEach { rowMapperFieldDef ->

            when (rowMapperFieldDef) {
                is EntityFieldRowMapperFieldDef -> `render for Entity field`(rowMapperFieldDef)
                is ForeignKeyRowMapperFieldDef -> `render for ForeignKey field`(rowMapperFieldDef)
                is ManyToManyRowMapperFieldDef -> `render for ManyToMany field`(rowMapperFieldDef)
            }

        }

        append(
            """
                |        )
                |
                |    }
                |""".trimMargin()
        )
    }


    private fun `render for Entity field`(rowMapperFieldDef: EntityFieldRowMapperFieldDef) {

        val line = renderRowMapperField(rowMapperFieldDef, indentSize = 12, orElseText = "", ::addImportFor)
        appendLine("$line,")

    }


    private fun `render for ForeignKey field`(rowMapperFieldDef: ForeignKeyRowMapperFieldDef) {

        val foreignKeyFieldDef = rowMapperFieldDef.foreignKeyFieldDef
        val pkAndNameDef = foreignKeyFieldDef.foreignEntityDef.entityPkAndNameDef
        val pkEntityFieldDef = pkAndNameDef.pkEntityFieldDef

        val idResultSetFieldName = "${foreignKeyFieldDef.foreignKeyFieldName}Id"
        val nameResultSetFieldName = "${foreignKeyFieldDef.foreignKeyFieldName}Name"

        addImportFor(pkAndNameDef.dtoDef.fqcn)

        appendLine("            ${pkAndNameDef.dtoUqcn}(")
        appendLine(renderRowMapperField(pkEntityFieldDef, idResultSetFieldName, nullable = false, indentSize = 16, orElseText = "", ::addImportFor) + ",")
        appendLine(renderRowMapperField(pkAndNameDef.nameEntityFieldDef, nameResultSetFieldName, nullable = false, indentSize = 16, orElseText = "(blank)", ::addImportFor) + ",")
        appendLine("            ),")

    }


    private fun `render for ManyToMany field`(rowMapperFieldDef: ManyToManyRowMapperFieldDef) {

        appendLine("            ${rowMapperFieldDef.classFieldName}PkAndNameDtoList,")

    }


    private fun `render functions for manyToManyPkAndNameDtos`() {

        val tripleQuote = "\"\"\""

        this.rowMapperDef.manyToManyFieldDefs.forEach { manyToManyRowMapperFieldDef ->

            addImportFor(Fqcns.MAIA_DOMAIN_ID)
            addImportFor(Fqcns.MAIA_SQL_PARAMS)

            append("""
                |
                |
                |    private fun fetch${manyToManyRowMapperFieldDef.classFieldName.firstToUpper()}PkAndNameDtos(entityId: DomainId): List<${manyToManyRowMapperFieldDef.entityPkAndNameDef.dtoUqcn}> {
                |
                |        return this.jdbcOps.queryForList(
                |            $tripleQuote
                |            select
                |""".trimMargin())

            appendLine("                other.id,")
            appendLine("                other.${manyToManyRowMapperFieldDef.otherSideEntity.entityDef.entityPkAndNameDef.nameEntityFieldDef.tableColumnName}")


            val otherSideEntity = manyToManyRowMapperFieldDef.otherSideEntity
            val otherSideIdTableColumnName = manyToManyRowMapperFieldDef.otherSideIdTableColumnName
            val thisSideIdTableColumnName = manyToManyRowMapperFieldDef.thisSideIdTableColumnName
            val manyToManyEntityDef = manyToManyRowMapperFieldDef.manyToManySearchableDtoFieldDef.manyToManyEntityDef

            append("""
                |            from ${otherSideEntity.entityDef.schemaAndTableName} other
                |            join ${manyToManyEntityDef.entityDef.schemaAndTableName} mtm
                |                on other.id = mtm.${otherSideIdTableColumnName}
                |            where mtm.${thisSideIdTableColumnName} = :entityId
                |            order by other.${otherSideEntity.entityDef.entityPkAndNameDef.nameEntityFieldDef.tableColumnName}
                |            $tripleQuote.trimIndent(),
                |            SqlParams().apply {
                |                addValue("entityId", entityId)
                |            },
                |            this.${manyToManyRowMapperFieldDef.classFieldName}PkAndNameDtoRowMapper
                |        )
                |
                |    }
                |""".trimMargin())



        }

    }


}
