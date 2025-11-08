package org.maiaframework.jdbc.sql.conditions

data class EqSqlCondition(private val databaseColumnName: String, private val value: Any?): SqlCondition {


    override fun toString(): String {
        return "$databaseColumnName = :$databaseColumnName"
    }

}
