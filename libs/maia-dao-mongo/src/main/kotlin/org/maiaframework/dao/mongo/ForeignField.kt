package org.maiaframework.dao.mongo

data class ForeignField(
        val dtoFieldName: String,
        val foreignEntityFieldName: String
)
