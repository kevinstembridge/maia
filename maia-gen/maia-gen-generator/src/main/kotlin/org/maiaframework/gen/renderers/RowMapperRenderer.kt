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

            if (manyToManyRowMapperFieldDef.joinFetchDtoDef == null) {
                val entityPkAndNameDef = manyToManyRowMapperFieldDef.entityPkAndNameDef

                addImportFor(entityPkAndNameDef.rowMapperDef.classDef.fqcn)

                append("""
                    |
                    |
                    |    private val ${manyToManyRowMapperFieldDef.classFieldName}PkAndNameDtoRowMapper = ${entityPkAndNameDef.rowMapperDef.classDef.uqcn}()
                    |""".trimMargin())
            }

        }

    }


    override fun renderFunctions() {

        `render function mapRow`()
        `render functions for manyToManyPkAndNameDtos`()

    }


    private fun `render function mapRow`() {

        addImportFor(Fqcns.MAIA_RESULT_SET_ADAPTER)
        addImportFor(rowMapperDef.rowFqcn)

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
                if (manyToManyRowMapperFieldDef.joinFetchDtoDef != null) {
                    appendLine("        val ${classFieldName}JoinFetchDtoList = fetch${classFieldName.firstToUpper()}JoinFetchDtos(entityId)")
                } else {
                    appendLine("        val ${classFieldName}PkAndNameDtoList = fetch${classFieldName.firstToUpper()}PkAndNameDtos(entityId)")
                }
                blankLine()
            }

        }

        val fieldNames = mutableListOf<String>()

        rowMapperDef.fieldDefs.forEach { rowMapperFieldDef ->

            when (rowMapperFieldDef) {
                is EntityFieldRowMapperFieldDef -> {
                    fieldNames.add(rowMapperFieldDef.classFieldName.value)
                    `render for Entity field`(rowMapperFieldDef)
                }
                is ForeignKeyRowMapperFieldDef -> {
                    fieldNames.add(rowMapperFieldDef.classFieldName.value)
                    `render for ForeignKey field`(rowMapperFieldDef)
                }
                is ManyToManyRowMapperFieldDef -> {
                    if (rowMapperFieldDef.joinFetchDtoDef != null) {
                        fieldNames.add("${rowMapperFieldDef.classFieldName}JoinFetchDtoList")
                    } else {
                        fieldNames.add("${rowMapperFieldDef.classFieldName}PkAndNameDtoList")
                    }
                }
            }

        }

        if (rowMapperDef.compositeIdFields.isNotEmpty()) {

            addImportRaw("java.net.URLEncoder.encode")
            fieldNames.add("id")

            val fieldCsv = rowMapperDef.compositeIdFields.joinToString(", ")

            blankLine()
            appendLine("""        val id = listOf($fieldCsv).joinToString(":") { encode(it.toString(), "UTF-8") }""")

        }

        blankLine()
        appendLine("        return ${rowMapperDef.rowUqcn}(")

        fieldNames
            .sorted()
            .forEach { fieldName ->
                appendLine("            $fieldName,")
            }

        append("""
            |        )
            |
            |    }
            |""".trimMargin()
        )

    }


    private fun `render for Entity field`(rowMapperFieldDef: EntityFieldRowMapperFieldDef) {

        val line = renderRowMapperField(rowMapperFieldDef, indentSize = 0, orElseText = "", ::addImportFor)
        appendLine("        val ${rowMapperFieldDef.classFieldName} = $line")

    }


    private fun `render for ForeignKey field`(rowMapperFieldDef: ForeignKeyRowMapperFieldDef) {

        val foreignKeyFieldDef = rowMapperFieldDef.foreignKeyFieldDef
        val pkAndNameDef = foreignKeyFieldDef.foreignEntityDef.entityPkAndNameDef
        val pkEntityFieldDef = pkAndNameDef.pkEntityFieldDef
        val nameEntityFieldDef = pkAndNameDef.nameEntityFieldDef

        val idResultSetFieldName = "${foreignKeyFieldDef.foreignKeyFieldName}Id"
        val nameResultSetFieldName = "${foreignKeyFieldDef.foreignKeyFieldName}Name"

        addImportFor(pkAndNameDef.dtoDef.fqcn)

        appendLine("        val ${rowMapperFieldDef.classFieldName} = ${pkAndNameDef.dtoUqcn}(")
        appendLine(renderRowMapperField(pkEntityFieldDef, idResultSetFieldName, nullable = false, indentSize = 12, orElseText = "", ::addImportFor) + ",")
        appendLine(renderRowMapperField(nameEntityFieldDef, nameResultSetFieldName, nullable = false, indentSize = 12, orElseText = "", ::addImportFor) + ",")
        appendLine("        )")

    }


    private fun `render functions for manyToManyPkAndNameDtos`() {

        val tripleQuote = "\"\"\""

        this.rowMapperDef.manyToManyFieldDefs.forEach { manyToManyRowMapperFieldDef ->

            val joinFetchDtoDef = manyToManyRowMapperFieldDef.joinFetchDtoDef
            val classFieldName = manyToManyRowMapperFieldDef.classFieldName

            if (joinFetchDtoDef != null) {

                addImportFor(Fqcns.MAIA_DOMAIN_ID)
                addImportFor(Fqcns.MAIA_SQL_PARAMS)
                addImportFor(joinFetchDtoDef.fqcn)

                append("""
                    |
                    |
                    |    private fun fetch${classFieldName.firstToUpper()}JoinFetchDtos(entityId: DomainId): List<${joinFetchDtoDef.uqcn}> {
                    |
                    |        return this.jdbcOps.queryForList(
                    |            $tripleQuote
                    |            select
                    |                mtm.id,
                    |                other.id as entity_id,
                    |                other.${joinFetchDtoDef.nameTableColumnName},
                    |                lower(mtm.effective_range) as effective_from,
                    |                upper(mtm.effective_range) as effective_to
                    |            from ${joinFetchDtoDef.otherSideEntitySchemaAndTableName} other
                    |            join ${joinFetchDtoDef.joinEntitySchemaAndTableName} mtm
                    |                on other.id = mtm.${joinFetchDtoDef.otherSideIdTableColumnName}
                    |            where mtm.${joinFetchDtoDef.thisSideIdTableColumnName} = :entityId
                    |            order by other.${joinFetchDtoDef.nameTableColumnName}
                    |            $tripleQuote.trimIndent(),
                    |            SqlParams().apply {
                    |                addValue("entityId", entityId)
                    |            },
                    |        ) { rsa ->
                    |            ${joinFetchDtoDef.uqcn}(
                    |                id = rsa.readDomainId("id"),
                    |                entityId = rsa.readDomainId("entity_id"),
                    |                name = rsa.readString("${joinFetchDtoDef.nameTableColumnName}"),
                    |                effectiveFrom = rsa.readInstantOrNull("effective_from"),
                    |                effectiveTo = rsa.readInstantOrNull("effective_to"),
                    |            )
                    |        }
                    |
                    |    }
                    |""".trimMargin())

            } else {

                addImportFor(Fqcns.MAIA_DOMAIN_ID)
                addImportFor(Fqcns.MAIA_SQL_PARAMS)
                val entityPkAndNameDef = manyToManyRowMapperFieldDef.entityPkAndNameDef
                addImportFor(entityPkAndNameDef.pkAndNameDtoFqcn)

                val otherSideEntity = manyToManyRowMapperFieldDef.otherSideEntity
                val otherSideIdTableColumnName = manyToManyRowMapperFieldDef.otherSideIdTableColumnName
                val thisSideIdTableColumnName = manyToManyRowMapperFieldDef.thisSideIdTableColumnName
                val manyToManyEntityDef = manyToManyRowMapperFieldDef.manyToManySearchableDtoFieldDef.manyToManyEntityDef

                append("""
                    |
                    |
                    |    private fun fetch${classFieldName.firstToUpper()}PkAndNameDtos(entityId: DomainId): List<${entityPkAndNameDef.pkAndNameDtoFqcn.uqcn}> {
                    |
                    |        return this.jdbcOps.queryForList(
                    |            $tripleQuote
                    |            select
                    |                other.id,
                    |                other.${otherSideEntity.entityDef.entityPkAndNameDef.nameEntityFieldDef.tableColumnName}
                    |            from ${otherSideEntity.entityDef.schemaAndTableName} other
                    |            join ${manyToManyEntityDef.entityDef.schemaAndTableName} mtm
                    |                on other.id = mtm.${otherSideIdTableColumnName}
                    |            where mtm.${thisSideIdTableColumnName} = :entityId
                    |            order by other.${otherSideEntity.entityDef.entityPkAndNameDef.nameEntityFieldDef.tableColumnName}
                    |            $tripleQuote.trimIndent(),
                    |            SqlParams().apply {
                    |                addValue("entityId", entityId)
                    |            },
                    |            this.${classFieldName}PkAndNameDtoRowMapper
                    |        )
                    |
                    |    }
                    |""".trimMargin())

            }

        }

    }


}
