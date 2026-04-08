package org.maiaframework.gen.renderers

import org.maiaframework.common.BlankStringException
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.ObjectIdFieldType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.UrlFieldType
import java.util.concurrent.atomic.AtomicInteger


class EntityFiltersRenderer(private val entityDef: EntityDef) : AbstractKotlinRenderer(entityDef.entityFiltersClassDef) {


    private val databaseFieldName: String = "databaseColumnName"


    override fun renderPreClassFields() {

        addImportFor(Fqcns.MAIA_JDBC_AND_OR)
        addImportFor(Fqcns.MAIA_JDBC_SQL_CONDITION_OPERATOR)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)
        addImportFor(AtomicInteger::class.java)

        blankLine()
        blankLine()
        appendLine("    private val sqlParamCounter = AtomicInteger(1)")

    }


    override fun renderFunctions() {

        val filterUqcn = this.entityDef.entityFilterClassDef.uqcn

        blankLine()
        blankLine()
        appendLine("    fun and(vararg filters: $filterUqcn): $filterUqcn {")
        blankLine()
        appendLine("        return IterableFunctionFilter(filters.toList(), AndOr.and)")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    fun or(vararg filters: $filterUqcn): $filterUqcn {")
        blankLine()
        appendLine("        return IterableFunctionFilter(filters.toList(), AndOr.or)")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    fun nor(vararg filters: $filterUqcn): $filterUqcn {")
        blankLine()
        appendLine("        return IterableFunctionFilter(filters.toList(), AndOr.nor)")
        blankLine()
        appendLine("    }")
        renderFieldFunctionsForJdbc()

    }


    private fun renderFieldFunctionsForJdbc() {


        this.entityDef.allEntityFieldsSorted.filterNot { it.classFieldDef.isMap }.forEach { fieldDef ->

            val classFieldDef = fieldDef.classFieldDef
            val fieldType = classFieldDef.fieldType

            addImportFor(fieldType)

            val returnType = if (classFieldDef.isList) "ListFieldFilter" else "FieldFilter"

            val uqcn = when (fieldType) {
                is ListFieldType -> fieldType.parameterFieldType.unqualifiedToString
                is SetFieldType -> fieldType.parameterFieldType.unqualifiedToString
                else -> classFieldDef.unqualifiedToString
            }

            blankLine()
            blankLine()
            appendLine("    val ${classFieldDef.classFieldName}: $returnType<$uqcn> ")
            appendLine("        get() {")
            blankLine()

            if (classFieldDef.isList) {
                appendLine("            return $returnType(\"${fieldDef.tableColumnName}\")")
            } else {

                addImportFor(Fqcns.SQL_TYPES)
                val valueMappingText = valueMappingTextFor(fieldType)
                appendLine("            return $returnType(\"${fieldDef.tableColumnName}\", Types.${fieldDef.fieldType.sqlType}, this.sqlParamCounter) { value -> $valueMappingText }")
            }
            blankLine()
            appendLine("        }")

        }



    }


    private fun valueMappingTextFor(fieldType: FieldType): String {

        return when (fieldType) {
            is BooleanFieldType -> "value"
            is BooleanTypeFieldType -> "value?.value"
            is BooleanValueClassFieldType -> "value?.value"
            is DataClassFieldType -> TODO("YAGNI?")
            is DomainIdFieldType -> "value?.value"
            is DoubleFieldType -> "value"
            is EnumFieldType -> "value?.name"
            is EsDocFieldType -> TODO("YAGNI?")
            is ForeignKeyFieldType -> "value?.value"
            is FqcnFieldType -> TODO("YAGNI?")
            is PkAndNameFieldType -> TODO("YAGNI?")
            is InstantFieldType -> {
                addImportFor(Fqcns.JAVA_SQL_TIMESTAMP)
                "value?.let { Timestamp.from(it) }"
            }
            is IntFieldType -> "value"
            is IntTypeFieldType -> "value?.value"
            is IntValueClassFieldType -> "value?.value"
            is ListFieldType -> TODO("YAGNI?")
            is LocalDateFieldType -> "value?.toString()"
            is LongFieldType -> "value"
            is LongTypeFieldType -> "value?.value"
            is MapFieldType -> TODO("YAGNI?")
            is ObjectIdFieldType -> TODO("YAGNI?")
            is PeriodFieldType -> "value?.toString()"
            is RequestDtoFieldType -> TODO("YAGNI?")
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> "value"
            is StringFieldType -> "value"
            is StringTypeFieldType -> "value?.value"
            is StringValueClassFieldType -> "value?.value"
            is UrlFieldType -> "value"
        }

    }


    override fun renderInnerClasses() {

        renderFieldFilterStaticClass()
        renderListFieldFilterStaticClass()
        renderNoOpFilterStaticClass()
        renderSimpleFunctionFilterStaticClass()
        renderMultiValueFunctionFilterStaticClass()
        renderIterableFunctionFilterStaticClass()
        renderJsonListFieldStaticClass()
        renderListFieldStaticClass()
        renderNullFiltersStaticClasses()

    }


    private fun renderFieldFilterStaticClass() {

        addImportFor(BlankStringException::class.java)

        blankLine()
        blankLine()
        appendLine("    class FieldFilter<T>(")
        appendLine("        private val $databaseFieldName: String,")
        appendLine("        private val sqlType: Int,")
        appendLine("        private val sqlParamCounter: AtomicInteger,")
        appendLine("        private val valueMappingFunc: (T?) -> Any?,")
        appendLine("    ) {")
        renderOperatorFilterMethod("eq", "EQUAL")
        renderOperatorFilterMethod("gt", "GREATER_THAN")
        renderOperatorFilterMethod("gte", "GREATER_THAN_OR_EQUAL")
        renderOperatorFilterMethod("lt", "LESS_THAN")
        renderOperatorFilterMethod("lte", "LESS_THAN_OR_EQUAL")
        renderOperatorFilterMethod("ne", "NOT_EQUAL")

        blankLine()
        blankLine()
        appendLine("        infix fun `in`(value: Iterable<T>): ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        appendLine("            return MultiValueFunctionFilter(this.$databaseFieldName, this.sqlType, this.sqlParamCounter, value, this.valueMappingFunc)")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        fun isNotNull(): ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        appendLine("            return IsNotNullFilter(this.databaseColumnName)")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        fun isNull(): ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        appendLine("            return IsNullFilter(this.databaseColumnName)")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        infix fun contains(value: T): ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        appendLine("            TODO(\"Not implemented yet\")")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("    }")

    }


    private fun renderOperatorFilterMethod(
        operator: String,
        sqlConditionOperator: String
    ) {

        blankLine()
        blankLine()
        appendLine("        infix fun $operator(value: T): ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        appendLine("            return SimpleFunctionFilter(")
        appendLine("                this.$databaseFieldName,")
        appendLine("                value,")
        appendLine("                this.sqlType,")
        appendLine("                this.sqlParamCounter,")
        appendLine("                SqlConditionOperator.${sqlConditionOperator},")
        appendLine("                this.valueMappingFunc")
        appendLine("            )")
        blankLine()
        appendLine("        }")

    }


    private fun renderListFieldFilterStaticClass() {

        if (entityDef.allEntityFieldsSorted.none { it.classFieldDef.isList }) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    class ListFieldFilter<T>(private val databaseColumnName: String) { ")
        blankLine()
        blankLine()
        appendLine("        infix fun contains(value: T): ${entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        appendLine("            return ListFieldContainsFilter(this.databaseColumnName, value)")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("    }")

    }


    private fun renderSimpleFunctionFilterStaticClass() {

        blankLine()
        blankLine()
        appendLine("    private class SimpleFunctionFilter<VALUE>(")
        appendLine("        private val fieldName: String,")
        appendLine("        private val value: VALUE,")
        appendLine("        private val sqlType: Int,")
        appendLine("        sqlParamCounter: AtomicInteger,")
        appendLine("        private val sqlConditionOperator: SqlConditionOperator,")
        appendLine("        private val valueMappingFunc: (VALUE?) -> Any?")
        appendLine("    ) : ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        blankLine()
        appendLine($$"        private val sqlParamName = \"${fieldName}_${sqlParamCounter.getAndIncrement()}\"")
        blankLine()
        blankLine()
        appendLine("        init {")
        blankLine()
        appendLine("            BlankStringException.throwIfBlank(fieldName, \"fieldName\")")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        override fun whereClause(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): String {")
        blankLine()
        appendLine($$"            return \"$fieldName ${operatorFor(sqlConditionOperator)} :${this.sqlParamName}\"")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        private fun operatorFor(sqlConditionOperator: SqlConditionOperator): String {")
        blankLine()
        appendLine("            return when (sqlConditionOperator) {")
        appendLine("                SqlConditionOperator.EQUAL -> \"=\"")
        appendLine("                SqlConditionOperator.NOT_EQUAL -> \"!=\"")
        appendLine("                SqlConditionOperator.GREATER_THAN -> \">\"")
        appendLine("                SqlConditionOperator.GREATER_THAN_OR_EQUAL -> \">=\"")
        appendLine("                SqlConditionOperator.LESS_THAN -> \"<\"")
        appendLine("                SqlConditionOperator.LESS_THAN_OR_EQUAL -> \"<=\"")
        appendLine("                SqlConditionOperator.LIKE -> \"like\"")
        appendLine("            }")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        override fun populateSqlParams(sqlParams: SqlParams) {")
        blankLine()
        appendLine("            val transformedValue = this.valueMappingFunc.invoke(this.value)")
        appendLine("            sqlParams.addValue(this.sqlParamName, transformedValue, this.sqlType)")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("    }")

    }


    private fun renderMultiValueFunctionFilterStaticClass() {

        blankLine()
        blankLine()
        appendLine("    private class MultiValueFunctionFilter<VALUE>(")
        appendLine("        private val fieldName: String,")
        appendLine("        private val sqlType: Int,")
        appendLine("        sqlParamCounter: AtomicInteger,")
        appendLine("        private val values: Iterable<VALUE>,")
        appendLine("        private val valueMappingFunc: (VALUE?) -> Any?")
        appendLine("    ) : ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        blankLine()
        appendLine($$"        private val sqlParamName = \"${fieldName}_${sqlParamCounter.getAndIncrement()}\"")
        blankLine()
        blankLine()
        appendLine("        init {")
        blankLine()
        appendLine("            BlankStringException.throwIfBlank(fieldName, \"fieldName\")")
        appendLine("            require(values.toList().isNotEmpty()) { \"The provided list of values must not be empty.\" }")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        override fun whereClause(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): String {")
        blankLine()
        appendLine($$"            return \"$fieldName in (:$sqlParamName)\"")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        override fun populateSqlParams(sqlParams: SqlParams) {")
        blankLine()
        appendLine("            val mappedValues = this.values.map(valueMappingFunc)")
        appendLine("            sqlParams.addValue(this.sqlParamName, mappedValues, this.sqlType)")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("    }")

    }


    private fun renderIterableFunctionFilterStaticClass() {

        val filterUqcn = this.entityDef.entityFilterClassDef.uqcn

        blankLine()
        blankLine()
        appendLine("    private class IterableFunctionFilter(")
        appendLine("        private val filters: List<$filterUqcn>,")
        appendLine("        private val andOr: AndOr")
        appendLine("    ) : $filterUqcn {")
        blankLine()
        blankLine()
        appendLine("        override fun whereClause(fieldConverter: ${this.entityDef.entityFieldConverterClassDef.uqcn}): String {")
        blankLine()
        appendLine($$"            return this.filters.map { it.whereClause(fieldConverter) }.joinToString(\" ${andOr.name} \")")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        override fun populateSqlParams(sqlParams: SqlParams) {")
        blankLine()
        appendLine("            this.filters.forEach { it.populateSqlParams(sqlParams) }")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("    }")

    }


    private fun renderJsonListFieldStaticClass() {

        if (entityDef.allEntityFieldsSorted.none { it.classFieldDef.isList }) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    private class JsonListFieldContainsFilter<VALUE>(")
        appendLine("        private val databaseColumnName: String,")
        appendLine("        private val value: VALUE")
        appendLine("    ): ${entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        blankLine()
        appendLine("        override fun whereClause(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): String {")
        blankLine()
        appendLine($$"            return \"jsonb_path_exists($databaseColumnName, '$ ? (@ == \\\"$value\\\")')\"")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        override fun populateSqlParams(sqlParams: SqlParams) {")
        blankLine()
        appendLine("            // do nothing")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("    }")

    }


    private fun renderListFieldStaticClass() {

        if (entityDef.allEntityFieldsSorted.none { it.classFieldDef.isList }) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    private class ListFieldContainsFilter<VALUE>(")
        appendLine("        private val databaseColumnName: String,")
        appendLine("        private val value: VALUE")
        appendLine("    ): ${entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        blankLine()
        appendLine("        override fun whereClause(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): String {")
        blankLine()
        appendLine($$"            return \"'$value' = ANY($databaseColumnName)\"")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("        override fun populateSqlParams(sqlParams: SqlParams) {")
        blankLine()
        appendLine("            // do nothing")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("    }")

    }


    private fun renderNullFiltersStaticClasses() {

        append(
            $$"""
            |
            |
            |    private class IsNullFilter(
            |        private val databaseColumnName: String
            |    ) : $${entityDef.entityFilterClassDef.uqcn} {
            |
            |
            |        override fun whereClause(fieldConverter: $${entityDef.entityFieldConverterClassDef.uqcn}): String {
            |
            |            return "$databaseColumnName is null"
            |
            |        }
            |
            |
            |        override fun populateSqlParams(sqlParams: SqlParams) {
            |
            |            // do nothing
            |
            |        }
            |
            |
            |    }
            |
            |
            |    private class IsNotNullFilter(
            |        private val databaseColumnName: String
            |    ) : $${entityDef.entityFilterClassDef.uqcn} {
            |
            |
            |        override fun whereClause(fieldConverter: $${entityDef.entityFieldConverterClassDef.uqcn}): String {
            |
            |            return "$databaseColumnName is not null"
            |
            |        }
            |
            |
            |        override fun populateSqlParams(sqlParams: SqlParams) {
            |
            |            // do nothing
            |
            |        }
            |
            |
            |    }
            |""".trimMargin()
        )

    }


    private fun renderNoOpFilterStaticClass() {

        append("""
            |
            |
            |    class NoopFilter : ${entityDef.entityFilterClassDef.uqcn} {
            |
            |
            |        override fun whereClause(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): String {
            |            return "1 = 1"
            |        }
            |
            |
            |        override fun populateSqlParams(sqlParams: SqlParams) {
            |            // Do nothing
            |        }
            |
            |
            |    }
            |""".trimMargin())

    }


}
