package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.lang.FieldType

object FieldTypeRendererHelper {


    fun determineSqlDataType(fieldType: FieldType): String {

        return fieldType.jdbcCompatibleType.postgresDataType

    }


}
