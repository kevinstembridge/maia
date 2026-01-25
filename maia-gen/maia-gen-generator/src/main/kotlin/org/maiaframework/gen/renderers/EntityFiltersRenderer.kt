package org.maiaframework.gen.renderers

import org.maiaframework.common.BlankStringException
import org.maiaframework.gen.spec.definition.DatabaseType
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
import org.maiaframework.gen.spec.definition.lang.IdAndNameFieldType
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


    private val sqlConditionOrBson: String = when (entityDef.databaseType) {
        DatabaseType.JDBC -> "SqlCondition"
        DatabaseType.MONGO -> "Bson"
    }


    private val sqlConditionsOrFilters: String = when (entityDef.databaseType) {
        DatabaseType.JDBC -> "SqlConditions"
        DatabaseType.MONGO -> "Filters"
    }


    private val databaseFieldName: String = when (entityDef.databaseType) {
        DatabaseType.JDBC -> "databaseColumnName"
        DatabaseType.MONGO -> "collectionFieldName"
    }


    override fun renderPreClassFields() {

        when (entityDef.databaseType) {
            DatabaseType.JDBC -> {
                addImportFor(Fqcns.MAIA_JDBC_AND_OR)
                addImportFor(Fqcns.MAIA_JDBC_SQL_CONDITION_OPERATOR)
                addImportFor(Fqcns.MAIA_SQL_PARAMS)
                addImportFor(AtomicInteger::class.java)
            }

            DatabaseType.MONGO -> {
                addImportFor(Fqcns.BSON)
                addImportFor(Fqcns.BSON_DOCUMENT)
                addImportFor(Fqcns.MONGO_FILTERS)
            }
        }

        if (entityDef.databaseType == DatabaseType.MONGO) {
            blankLine()
            appendLine("    val emptyFilter = object: ${this.entityDef.entityFilterClassDef.uqcn} {")
            appendLine("        override fun asBson(fieldConverter: ${this.entityDef.entityFieldConverterClassDef.uqcn}): Bson {")
            appendLine("            return Document()")
            appendLine("        }")
            appendLine("    }")
        }

        if (entityDef.databaseType == DatabaseType.JDBC) {

            blankLine()
            blankLine()
            appendLine("    private val sqlParamCounter = AtomicInteger(1)")

        }

    }


    override fun renderFunctions() {

        val filterUqcn = this.entityDef.entityFilterClassDef.uqcn

        blankLine()
        blankLine()
        appendLine("    fun and(vararg filters: $filterUqcn): $filterUqcn {")
        blankLine()

        when (entityDef.databaseType) {
            DatabaseType.JDBC -> appendLine("        return IterableFunctionFilter(filters.toList(), AndOr.and)")
            DatabaseType.MONGO -> appendLine("        return IterableFunctionFilter(filters.toList()) { $sqlConditionsOrFilters.and(it) }")
        }

        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    fun or(vararg filters: $filterUqcn): $filterUqcn {")
        blankLine()

        when (entityDef.databaseType) {
            DatabaseType.JDBC -> appendLine("        return IterableFunctionFilter(filters.toList(), AndOr.or)")
            DatabaseType.MONGO -> appendLine("        return IterableFunctionFilter(filters.toList()) { $sqlConditionsOrFilters.or(it) }")
        }

        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    fun nor(vararg filters: $filterUqcn): $filterUqcn {")
        blankLine()

        when (entityDef.databaseType) {
            DatabaseType.JDBC -> appendLine("        return IterableFunctionFilter(filters.toList(), AndOr.nor)")
            DatabaseType.MONGO -> appendLine("        return IterableFunctionFilter(filters.toList()) { $sqlConditionsOrFilters.nor(it) }")
        }

        blankLine()
        appendLine("    }")

        when (entityDef.databaseType) {
            DatabaseType.JDBC -> renderFieldFunctionsForJdbc()
            DatabaseType.MONGO -> renderFieldFunctionsForMongo()
        }

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
            is IdAndNameFieldType -> TODO("YAGNI?")
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


    private fun renderFieldFunctionsForMongo() {


        this.entityDef.allEntityFieldsSorted.filterNot { it.classFieldDef.isMap }.forEach { fieldDef ->

            val classFieldDef = fieldDef.classFieldDef
            val fieldType = classFieldDef.fieldType

            addImportFor(fieldType)

            val uqcn = when (fieldType) {
                is ListFieldType -> fieldType.parameterFieldType.unqualifiedToString
                is SetFieldType -> fieldType.parameterFieldType.unqualifiedToString
                else -> classFieldDef.unqualifiedToString
            }

            blankLine()
            blankLine()
            appendLine("    fun ${classFieldDef.classFieldName}(): FieldFilter<$uqcn> {")
            blankLine()
            appendLine("        return FieldFilter(\"${fieldDef.tableColumnName}\")")
            blankLine()
            appendLine("    }")

        }

    }


    override fun renderInnerClasses() {

        renderFieldFilterStaticClass()
        renderListFieldFilterStaticClass()
        renderSimpleSupplierFilterStaticClass()
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
        when (entityDef.databaseType) {
            DatabaseType.JDBC -> {
                appendLine("    class FieldFilter<T>(")
                appendLine("        private val $databaseFieldName: String,")
                appendLine("        private val sqlType: Int,")
                appendLine("        private val sqlParamCounter: AtomicInteger,")
                appendLine("        private val valueMappingFunc: (T?) -> Any?,")
                appendLine("    ) {")
            }
            DatabaseType.MONGO -> appendLine("    class FieldFilter<T>(private val $databaseFieldName: String) {")
        }
        renderOperatorFilterMethod("eq", "EQUAL")
        renderOperatorFilterMethod("gt", "GREATER_THAN", nullable = false)
        renderOperatorFilterMethod("gte", "GREATER_THAN_OR_EQUAL", nullable = false)
        renderOperatorFilterMethod("lt", "LESS_THAN", nullable = false)
        renderOperatorFilterMethod("lte", "LESS_THAN_OR_EQUAL", nullable = false)
        renderOperatorFilterMethod("ne", "NOT_EQUAL")

        if (entityDef.databaseType == DatabaseType.MONGO) {

            blankLine()
            blankLine()
            appendLine("        fun `in`(value: Iterable<T>): ${this.entityDef.entityFilterClassDef.uqcn} {")
            blankLine()
            appendLine("            val bsonFunction: (List<*>) -> Bson = { convertedValues -> Filters.`in`(this.collectionFieldName, convertedValues) }")
            appendLine("            return MultiValueFunctionFilter(this.collectionFieldName, value, bsonFunction)")
            blankLine()
            appendLine("        }")
            blankLine()
            blankLine()
            appendLine("        fun exists(): ${this.entityDef.entityFilterClassDef.uqcn} {")
            blankLine()
            appendLine("            return SimpleSupplierFilter({ Filters.exists(this.$databaseFieldName) })")
            blankLine()
            appendLine("        }")
            blankLine()
            blankLine()
            appendLine("        fun notExists(): ${this.entityDef.entityFilterClassDef.uqcn} {")
            blankLine()
            appendLine("            return SimpleSupplierFilter({ Filters.not(Filters.exists(this.$databaseFieldName)) })")
            blankLine()
            appendLine("        }")
        }

        if (entityDef.databaseType == DatabaseType.JDBC) {

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
        }

        blankLine()
        blankLine()
        appendLine("        infix fun contains(value: T): ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()

        when (entityDef.databaseType) {
            DatabaseType.JDBC -> {
                appendLine("            TODO(\"Not implemented yet\")")
            }
            DatabaseType.MONGO -> {
                appendLine("            return SimpleFunctionFilter(")
                appendLine("                this.$databaseFieldName,")
                appendLine("                value,")
                appendLine("                { convertedValue ->")
                appendLine("                    val filter = Document(\"\\\$eq\", convertedValue)")
                appendLine("                    Filters.elemMatch($databaseFieldName, filter)")
                appendLine("                }")
                appendLine("            )")
            }
        }

        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        appendLine("    }")

    }


    private fun renderOperatorFilterMethod(
        operator: String,
        sqlConditionOperator: String,
        nullable: Boolean = true
    ) {

        val nullableSuffix = if (nullable) "" else "!!"

        blankLine()
        blankLine()
        appendLine("        infix fun $operator(value: T): ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        when (entityDef.databaseType) {
            DatabaseType.JDBC -> {
                appendLine("            return SimpleFunctionFilter(")
                appendLine("                this.$databaseFieldName,")
                appendLine("                value,")
                appendLine("                this.sqlType,")
                appendLine("                this.sqlParamCounter,")
                appendLine("                SqlConditionOperator.${sqlConditionOperator},")
                appendLine("                this.valueMappingFunc")
                appendLine("            )")
            }
            DatabaseType.MONGO -> appendLine("            return SimpleFunctionFilter(this.$databaseFieldName, value) { convertedValue -> Filters.$operator(this.$databaseFieldName, convertedValue$nullableSuffix) }")
        }
        blankLine()
        appendLine("        }")

    }


    private fun renderListFieldFilterStaticClass() {

        if (entityDef.databaseType == DatabaseType.MONGO) {
            return
        }

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


    private fun renderSimpleSupplierFilterStaticClass() {

        if (entityDef.databaseType != DatabaseType.MONGO) return

        blankLine()
        blankLine()
        appendLine("    private class SimpleSupplierFilter(private val supplier: () -> ${sqlConditionOrBson}): ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        blankLine()
        appendLine("        override fun as${sqlConditionOrBson}(fieldConverter: ${this.entityDef.entityFieldConverterClassDef.uqcn}): $sqlConditionOrBson {")
        blankLine()
        appendLine("            return this.supplier.invoke()")
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
        when (entityDef.databaseType) {
            DatabaseType.JDBC -> {
                appendLine("        private val sqlType: Int,")
                appendLine("        sqlParamCounter: AtomicInteger,")
                appendLine("        private val sqlConditionOperator: SqlConditionOperator,")
                appendLine("        private val valueMappingFunc: (VALUE?) -> Any?")
            }

            DatabaseType.MONGO -> appendLine("        private val bsonFunction: (Any?) -> Bson")
        }
        appendLine("    ) : ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        blankLine()
        appendLine("        private val sqlParamName = \"\${fieldName}_\${sqlParamCounter.getAndIncrement()}\"")
        blankLine()
        blankLine()
        appendLine("        init {")
        blankLine()
        appendLine("            BlankStringException.throwIfBlank(fieldName, \"fieldName\")")
        blankLine()
        appendLine("        }")
        blankLine()
        blankLine()
        when (entityDef.databaseType) {
            DatabaseType.JDBC -> {
                appendLine("        override fun whereClause(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): String {")
                blankLine()
                appendLine("            return \"\$fieldName \${operatorFor(sqlConditionOperator)} :\${this.sqlParamName}\"")
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
            }
            DatabaseType.MONGO -> {
                appendLine("        override fun asBson(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): Bson {")
                blankLine()
                appendLine("            val convertedValue = fieldConverter.convert(this.fieldName, this.value)")
                appendLine("            return this.bsonFunction.invoke(convertedValue)")
                blankLine()
                appendLine("        }")
            }
        }
        blankLine()
        blankLine()
        appendLine("    }")

    }


    private fun renderMultiValueFunctionFilterStaticClass() {

        blankLine()
        blankLine()
        appendLine("    private class MultiValueFunctionFilter<VALUE>(")
        appendLine("        private val fieldName: String,")

        when (entityDef.databaseType) {
            DatabaseType.JDBC -> {
                appendLine("        private val sqlType: Int,")
                appendLine("        sqlParamCounter: AtomicInteger,")
                appendLine("        private val values: Iterable<VALUE>,")
                appendLine("        private val valueMappingFunc: (VALUE?) -> Any?")
            }
            DatabaseType.MONGO -> {
                appendLine("        private val values: Iterable<VALUE>,")
                appendLine("        private val bsonFunction: (List<*>) -> Bson")
            }
        }

        appendLine("    ) : ${this.entityDef.entityFilterClassDef.uqcn} {")
        blankLine()
        blankLine()
        appendLine("        private val sqlParamName = \"\${fieldName}_\${sqlParamCounter.getAndIncrement()}\"")
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
        when (entityDef.databaseType) {
            DatabaseType.JDBC -> {
                appendLine("        override fun whereClause(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): String {")
                blankLine()
                appendLine("            return \"\$fieldName in (:\$sqlParamName)\"")
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
            }
            DatabaseType.MONGO -> {
                appendLine("        override fun asBson(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): Bson {")
                blankLine()
                appendLine("            val convertedValues = this.values.map { fieldConverter.convert(this.fieldName, it) }")
                appendLine("            return this.bsonFunction.invoke(convertedValues)")
                blankLine()
                appendLine("        }")
            }
        }
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

        when (entityDef.databaseType) {
            DatabaseType.JDBC -> appendLine("        private val andOr: AndOr")
            DatabaseType.MONGO -> appendLine("        private val bsonFunction: (List<${sqlConditionOrBson}>) -> $sqlConditionOrBson")
        }

        appendLine("    ) : $filterUqcn {")
        blankLine()
        blankLine()
        when (entityDef.databaseType) {
            DatabaseType.JDBC -> {
                appendLine("        override fun whereClause(fieldConverter: ${this.entityDef.entityFieldConverterClassDef.uqcn}): String {")
                blankLine()
                appendLine("            return this.filters.map { it.whereClause(fieldConverter) }.joinToString(\" \${andOr.name} \")")
                blankLine()
                appendLine("        }")
                blankLine()
                blankLine()
                appendLine("        override fun populateSqlParams(sqlParams: SqlParams) {")
                blankLine()
                appendLine("            this.filters.forEach { it.populateSqlParams(sqlParams) }")
                blankLine()
                appendLine("        }")
            }
            DatabaseType.MONGO -> {
                appendLine("        override fun asBson(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): Bson {")
                blankLine()
                appendLine("            val bsons = this.filters.map { filter -> filter.asBson(fieldConverter) }")
                appendLine("            return this.bsonFunction.invoke(bsons)")
                blankLine()
                appendLine("        }")
            }
        }
        blankLine()
        blankLine()
        appendLine("    }")

    }


    private fun renderJsonListFieldStaticClass() {

        if (entityDef.databaseType == DatabaseType.MONGO || entityDef.allEntityFieldsSorted.none { it.classFieldDef.isList }) {
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
        appendLine("            return \"jsonb_path_exists(\$databaseColumnName, '\$ ? (@ == \\\"\$value\\\")')\"")
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

        if (entityDef.databaseType == DatabaseType.MONGO || entityDef.allEntityFieldsSorted.none { it.classFieldDef.isList }) {
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
        appendLine("            return \"'\$value' = ANY(\$databaseColumnName)\"")
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

        if (entityDef.databaseType != DatabaseType.JDBC) {
            return
        }

        append("""
            |
            |
            |    private class IsNullFilter(
            |        private val databaseColumnName: String
            |    ) : ${entityDef.entityFilterClassDef.uqcn} {
            |
            |
            |        override fun whereClause(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): String {
            |
            |            return "${"$"}databaseColumnName is null"
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
            |    ) : ${entityDef.entityFilterClassDef.uqcn} {
            |
            |
            |        override fun whereClause(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): String {
            |
            |            return "${"$"}databaseColumnName is not null"
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
