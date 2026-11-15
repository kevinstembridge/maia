package org.maiaframework.gen.schema.expected

import org.maiaframework.gen.spec.definition.jdbc.TableColumnName


data class ExpectedColumnDef(
    val name: TableColumnName,
    val postgresType: String,
    val nullable: Boolean,
    val isPrimaryKey: Boolean,
    /**
     * The type as written in generated DDL. Differs from [postgresType] (the schema-check comparison type)
     * where the DDL carries more detail, e.g. `NUMERIC(19,4)` versus `decimal`.
     */
    val ddlType: String = postgresType,
)
