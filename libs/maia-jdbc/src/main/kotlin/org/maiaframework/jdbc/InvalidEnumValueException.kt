package org.maiaframework.jdbc


class InvalidEnumValueException(
        val invalidValue: String,
        val columnName: String,
        val enumClass: Class<*>
) : MaiaDataAccessException("Invalid enum value [$invalidValue] in column $columnName. Expected one from ${enumClass.canonicalName}")
