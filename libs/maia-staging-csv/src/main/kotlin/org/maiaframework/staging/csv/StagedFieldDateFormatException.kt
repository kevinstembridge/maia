package org.maiaframework.staging.csv

import java.time.format.DateTimeParseException

class StagedFieldDateFormatException(
    val stagedFieldMeta: StagedFieldMeta,
    val expectedDateFormat: String,
    dateTimeParseException: DateTimeParseException
): RuntimeException(
    "Expected a date field in format $expectedDateFormat but found: $stagedFieldMeta",
    dateTimeParseException
)
