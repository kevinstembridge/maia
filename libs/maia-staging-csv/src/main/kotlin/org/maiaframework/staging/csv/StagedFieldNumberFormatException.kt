package org.maiaframework.staging.csv

class StagedFieldNumberFormatException(
    val stagedFieldMeta: StagedFieldMeta
): RuntimeException(
    "Expected a numeric value but found: $stagedFieldMeta"
)
