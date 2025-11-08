package org.maiaframework.jdbc

import io.micrometer.core.annotation.Timed
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.PreparedStatementCallback
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import java.util.stream.Stream
import kotlin.streams.asSequence


class JdbcOps(private val jdbcOps: NamedParameterJdbcOperations) {


    @Timed
    fun <ITEM> insert(
            sql: String,
            item: ITEM,
            paramSourceMapper: SqlParameterSourceMapper<ITEM>
    ) {

        val parameterSource = SqlParams()
        paramSourceMapper.populateParameterSourceMapper(item, parameterSource)

        jdbcOpsUpdate(sql, parameterSource)

    }


    @Timed
    fun insert(
            sql: String,
            sqlParams: SqlParams
    ) {

        jdbcOpsUpdate(sql, sqlParams)

    }


    @Timed
    fun queryForInt(
        sql: String,
        params: SqlParams = SqlParams()
    ): Int {

        return queryForObjectOrNull(sql, params) { rsa -> rsa.readInt(1) } ?: 0

    }


    @Timed
    fun queryForLong(
        sql: String,
        params: SqlParams = SqlParams()
    ): Long {

        return queryForObjectOrNull(sql, params) { rsa -> rsa.readLong(1) } ?: 0

    }


    @Timed
    fun <T> queryForObjectOrNull(
        sql: String,
        queryParameterSource: SqlParams,
        rowMapper: MaiaRowMapper<T>
    ): T? {

        return try {
            jdbcOpsQueryForObject(sql, queryParameterSource, rowMapper, logErrors = false)
        } catch (e: IncorrectResultSizeDataAccessException) {

            if (e.actualSize > 1) {
                throw e
            } else {
                null
            }

        } catch (e: EmptyResultDataAccessException) {
            null
        }

    }


    @Timed
    fun update(sql: String): Int {
        return update(sql, SqlParams())
    }


    @Timed
    fun update(sql: String, sqlParams: SqlParams): Int {

        traceLog(sql, sqlParams)

        try {
            return this.jdbcOps.update(sql, sqlParams.underlying)
        } catch (e: MaiaDataAccessException) {
            errorLog(sql, sqlParams, e)
            throw e
        } catch (e: DataAccessException) {
            errorLog(sql, sqlParams, e)
            throw e
        }

    }


    @Timed
    fun <T> execute(
        sql: String,
        sqlParams: SqlParams,
        preparedStatementCallback: PreparedStatementCallback<T>
    ): T? {

        traceLog(sql, sqlParams)

        try {
            return this.jdbcOps.execute(sql, sqlParams.underlying, preparedStatementCallback)
        } catch (e: MaiaDataAccessException) {
            errorLog(sql, sqlParams, e)
            throw e
        } catch (e: DataAccessException) {
            errorLog(sql, sqlParams, e)
            throw e
        }

    }


    private fun <T> jdbcOpsQueryForObject(
        sql: String,
        sqlParams: SqlParams,
        rowMapper: MaiaRowMapper<T>,
        logErrors: Boolean = true
    ): T {

        traceLog(sql, sqlParams)

        try {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            return this.jdbcOps.queryForObject(sql, sqlParams.underlying, adapt(rowMapper))
        } catch (e: MaiaDataAccessException) {
            if (logErrors) {
                errorLog(sql, sqlParams, e)
            }
            throw e
        } catch (e: DataAccessException) {
            if (logErrors) {
                errorLog(sql, sqlParams, e)
            }
            throw e
        }

    }


    private fun <ROW_TYPE> adapt(rowMapper: MaiaRowMapper<ROW_TYPE>): SpringRowMapperAdapter<ROW_TYPE> {
        return SpringRowMapperAdapter(rowMapper)
    }


    @Timed
    fun <T> queryForSequence(
        sql: String,
        sqlParams: SqlParams,
        rowMapper: MaiaRowMapper<T>
    ): Sequence<T> {

        traceLog(sql, sqlParams)

        try {

            // This trick is a way to automatically close the Stream, because the flatMap function does the closing for us.
            return Stream.of("ignore")
                .flatMap { this.jdbcOps.queryForStream(sql, sqlParams.underlying, adapt(rowMapper)) }
                .asSequence()

        } catch (e: DataAccessException) {
            throw MaiaSqlException("Error executing query", sql, sqlParams, e)
        }

    }


    private fun jdbcOpsUpdate(
        sql: String,
        sqlParams: SqlParams,
    ) {

        traceLog(sql, sqlParams)
        try {
            this.jdbcOps.update(sql, sqlParams.underlying)
        } catch (e: MaiaDataAccessException) {
            errorLog(sql, sqlParams, e)
            throw e
        } catch (e: DataAccessException) {
            errorLog(sql, sqlParams, e)
            throw e
        }


    }


    @Timed
    fun batchUpdate(sql: String, sqlParamsList: List<SqlParams>): IntArray {
        return batchUpdateWithParameterSources(sql, sqlParamsList.map { it.underlying })
    }


    private fun batchUpdateWithParameterSources(
        sql: String,
        sqlParamsList: List<SqlParameterSource>
    ): IntArray {

        traceLogBulkInsert(sql, sqlParamsList.size)

        try {
            return jdbcOps.batchUpdate(sql, sqlParamsList.toTypedArray())
        } catch (e: MaiaDataAccessException) {
            errorLog(sql, sqlParamsList, e)
            throw e
        } catch (e: DataAccessException) {
            errorLog(sql, sqlParamsList, e)
            throw e
        }

    }


    @Timed
    fun <T> queryForList(
        sql: String,
        sqlParams: SqlParams,
        rowMapper: MaiaRowMapper<T>
    ): List<T> {

        traceLog(sql, sqlParams)

        try {
            return jdbcOps.query(sql, sqlParams.underlying) { resultSet, idx ->
                rowMapper.mapRow(ResultSetAdapter(resultSet))
            }
        } catch (e: MaiaDataAccessException) {
            errorLog(sql, sqlParams, e)
            throw e
        } catch (e: DataAccessException) {
            errorLog(sql, sqlParams, e)
            throw e
        }

    }


    private fun traceLogBulkInsert(sql: String, itemCount: Int) {
        JDBC_OPS_LOGGER.trace("sql: {}, itemCount: {}", sql, itemCount)
    }


    private fun traceLog(sql: String, sqlParams: SqlParams) {
        JDBC_OPS_LOGGER.trace("sql: {}, params: {}", sql, sqlParams)
    }


    private fun errorLog(sql: String, sqlParams: SqlParams, e: Exception) {
        JDBC_OPS_LOGGER.error("sql: $sql, params: $sqlParams", e)
    }


    private fun errorLog(sql: String, sqlParams: List<SqlParameterSource>, e: Exception) {

        if (JDBC_OPS_LOGGER.isErrorEnabled) {
            JDBC_OPS_LOGGER.error("sql: $sql", e)
            JDBC_OPS_LOGGER.error("params: $sqlParams")
        }

    }


    companion object {

        private val JDBC_OPS_LOGGER = LoggerFactory.getLogger("JDBC_OPS_LOGGER")

    }


}
