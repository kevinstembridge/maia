package org.maiaframework.gen.spec.definition.mongo


import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.types.StringType

class CollectionFieldName(value: String) : StringType<CollectionFieldName>(value) {


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


        val changeType = CollectionFieldName("change_type")


        val createdById = CollectionFieldName("created_by_id")


        val createdByName = CollectionFieldName("created_by_name")


        val createdTimestampUtc = CollectionFieldName("c_ts")


        val entityId = CollectionFieldName("entityId")


        fun id(databaseType: DatabaseType) = when (databaseType) {
            DatabaseType.MONGO -> CollectionFieldName("_id")
            DatabaseType.JDBC -> CollectionFieldName("id")
        }


        val lastModifiedById = CollectionFieldName("lm_by_id")


        val lastModifiedByName = CollectionFieldName("lm_by_name")


        val lastModifiedTimestampUtc = CollectionFieldName("lm_ts")


        val version = CollectionFieldName("v")


    }


}
