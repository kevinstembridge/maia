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


        val createdTimestampUtc = TableColumnName("created_timestamp_utc")


        fun id(databaseType: DatabaseType) = when (databaseType) {
            DatabaseType.MONGO -> TableColumnName("_id")
            DatabaseType.JDBC -> TableColumnName("id")
        }


        val id = TableColumnName("id")


        val lastModifiedById = TableColumnName("last_modified_by_id")


        val lastModifiedByName = TableColumnName("last_modified_by_name")


        val lastModifiedTimestampUtc = TableColumnName("last_modified_timestamp_utc")


        val lifecycleState = TableColumnName("lifecycle_state")


        val version = TableColumnName("version")


    }


}
