package org.maiaframework.staging.csv

class StagedFieldMissingException(
    val stagedFieldMeta: StagedFieldMeta
) : RuntimeException(
    "Missing a value for required staged field: $stagedFieldMeta"
)
