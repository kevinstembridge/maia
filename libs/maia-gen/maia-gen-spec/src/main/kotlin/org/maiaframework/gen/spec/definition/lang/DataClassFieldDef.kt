package org.maiaframework.gen.spec.definition.lang


class DataClassFieldDef(
    val classFieldDef: ClassFieldDef,
    val fieldReaderParameterizedType: ParameterizedType?,
    val fieldWriterParameterizedType: ParameterizedType?
) : Comparable<DataClassFieldDef> {


    val fieldType = this.classFieldDef.fieldType


    override fun compareTo(other: DataClassFieldDef): Int {

        return this.classFieldDef.compareTo(other.classFieldDef)

    }


}
