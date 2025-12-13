package org.maiaframework.gen.spec.definition.lang

import org.maiaframework.common.BlankStringException
import org.maiaframework.types.StringType

class ClassFieldName(value: String) : StringType<ClassFieldName>(value) {


    fun withSuffix(suffix: String): ClassFieldName {

        BlankStringException.throwIfBlank(suffix, "suffix")
        return ClassFieldName(value + suffix)

    }


    companion object {

        val changeType = ClassFieldName("changeType")

        val context = ClassFieldName("context")

        val createdBy = ClassFieldName("createdBy")

        val createdById = ClassFieldName("createdById")

        val createdByUsername = ClassFieldName("createdByUsername")

        val createdTimestampUtc = ClassFieldName("createdTimestampUtc")

        val id = ClassFieldName("id")

        val lastModifiedBy = "lastModifiedBy"

        val lastModifiedByUsername = ClassFieldName("lastModifiedByUsername")

        val lastModifiedById = ClassFieldName("lastModifiedById")

        val lastModifiedTimestampUtc = ClassFieldName("lastModifiedTimestampUtc")

        val lifecycleState = ClassFieldName("lifecycleState")

        val version = ClassFieldName("version")

    }


}
