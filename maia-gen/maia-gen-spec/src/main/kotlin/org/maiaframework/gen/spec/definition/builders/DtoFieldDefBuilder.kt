package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.SimpleResponseDtoFieldDef
import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.Nullability


class DtoFieldDefBuilder(
    private val classFieldName: ClassFieldName,
    private val fieldType: FieldType,
    private val caseSensitive: CaseSensitive
) {


    private var nullability = Nullability.NOT_NULLABLE
    private var isMasked = false


    fun nullable(): DtoFieldDefBuilder {

        this.nullability = Nullability.NULLABLE
        return this

    }


    fun build(): SimpleResponseDtoFieldDef {

        return SimpleResponseDtoFieldDef(
            this.classFieldName,
            this.fieldType,
            this.nullability,
            this.isMasked,
            this.caseSensitive
        )

    }


    fun masked(): DtoFieldDefBuilder {

        this.isMasked = true
        return this

    }


}
