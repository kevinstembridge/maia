package org.maiaframework.jdbc.sql.conditions

object SqlConditions {


    fun eq(databaseColumnName: String, value: Any?): SqlCondition {

        return EqSqlCondition(databaseColumnName, value)

    }


    fun and(conditions: List<SqlCondition>): SqlCondition {

        return AndSqlCondition(conditions)

    }


    fun or(conditions: List<SqlCondition>): SqlCondition {

        return OrSqlCondition(conditions)

    }


    fun nor(conditions: List<SqlCondition>): SqlCondition {

        return NorSqlCondition(conditions)

    }


    fun gt(databaseColumnName: String, value: Any): SqlCondition {

        return GtSqlCondition(databaseColumnName, value)

    }


    fun gte(databaseColumnName: String, value: Any): SqlCondition {

        return GteSqlCondition(databaseColumnName, value)

    }


    fun lt(databaseColumnName: String, value: Any): SqlCondition {

        return LtSqlCondition(databaseColumnName, value)

    }


    fun lte(databaseColumnName: String, value: Any): SqlCondition {

        return LteSqlCondition(databaseColumnName, value)

    }


    fun ne(databaseColumnName: String, value: Any?): SqlCondition {

        return NeSqlCondition(databaseColumnName, value)

    }


    fun `in`(databaseColumnName: String, values: List<*>): SqlCondition {

        return InSqlCondition(databaseColumnName, values)

    }


    fun contains(databaseColumnName: String, value: Any?): SqlCondition {

        return ContainsSqlCondition(databaseColumnName, value)

    }


}
