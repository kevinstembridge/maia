package org.maiaframework.problem

import org.springframework.http.HttpStatus
import java.net.URI

class MaiaProblems(
    private val typePrefixSupplier: () -> String
) {


    init {
        // Fail on startup if the property has not been configured
        typeFor("huh")
    }


    fun foreignKeyRecordsExist(foreignKeyEntityName: String): Throwable {

        return errorResponse(
            "foreign_key_record_exists",
            "Foreign Key Record Exists",
            "Foreign key reference exists to entity $foreignKeyEntityName",
            HttpStatus.CONFLICT,
        )

    }


    fun uniqueConstraintViolationErrorResponse(vararg fieldNameArray: String): Throwable {

        val fieldNames = fieldNameArray.toSortedSet()

        if (fieldNames.size > 1) {

            val fieldErrors = fieldNameArray.map { fieldName -> fieldName to "This field is part of a unique key." }
                .plus("_error" to "Unique key conflict.")

            return errorResponse(
                "unique_constraint_violation",
                "Unique Constraint Violation",
                "These fields are part of a unique key",
                HttpStatus.CONFLICT,
                properties = mapOf(
                    "validationErrors" to fieldErrors
                )
            )

        } else {

            return errorResponse(
                "unique_constraint_violation",
                "Unique Constraint Violation",
                "This value must be unique",
                HttpStatus.CONFLICT,
                properties = mapOf(
                    "validationErrors" to listOf(
                        mapOf(fieldNames.first() to "This value must be unique"),
                    )
                ))

        }

    }


    fun errorResponse(
        typeSuffix: String,
        title: String,
        detail: String,
        statusCode: HttpStatus,
        properties: Map<String, Any> = emptyMap()
    ): Throwable {

        return ProblemBuilder.errorResponse(
            typeFor(typeSuffix),
            title,
            detail,
            statusCode,
            properties
        )

    }


    private fun typeFor(typeSuffix: String): URI {

        val typePrefix = this.typePrefixSupplier.invoke()
        return URI("$typePrefix/$typeSuffix")

    }


}
