package org.maiaframework.gen.spec.definition.jdbc


import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.types.StringType

class TableColumnName(value: String) : StringType<TableColumnName>(value) {


    fun toValidJavaIdentifier(): String {

        val sb = StringBuilder()

        val chars = value.toCharArray()

        for (ch in chars) {

            if (Character.isJavaIdentifierPart(ch)) {
                sb.append(ch)
            } else {
                sb.append("_")
                sb.append(Character.hashCode(ch))
                sb.append("_")
            }

        }

        return sb.toString()

    }


    companion object {


        val changeType = TableColumnName("change_type")


        val createdById = TableColumnName("created_by_id")


        val createdByName = TableColumnName("created_by_name")


        val createdTimestampUtc = TableColumnName("c_ts")


        val entityId = TableColumnName("entityId")


        fun id(databaseType: DatabaseType) = when (databaseType) {
            DatabaseType.MONGO -> TableColumnName("_id")
            DatabaseType.JDBC -> TableColumnName("id")
        }


        val lastModifiedById = TableColumnName("lm_by_id")


        val lastModifiedByName = TableColumnName("lm_by_name")


        val lastModifiedTimestampUtc = TableColumnName("lm_ts")


        val id = TableColumnName("id")


        val version = TableColumnName("v")


    }


}
