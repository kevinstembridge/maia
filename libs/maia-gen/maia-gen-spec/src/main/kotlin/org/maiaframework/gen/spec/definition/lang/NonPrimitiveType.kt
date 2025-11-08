package org.maiaframework.gen.spec.definition.lang

abstract class NonPrimitiveType protected constructor(
    val fqcn: Fqcn,
    val unqualifiedToString: String
) {


    val uqcn = fqcn.uqcn


    abstract val isEnum: Boolean


    abstract val isString: Boolean


    abstract val simpleTypeUnderlyingFieldType: FieldType?


    abstract val isSetOfEnums: Boolean


    abstract fun asParameterTo(fqcn: Fqcn): ParameterizedType


    abstract fun withPrefix(prefix: String): NonPrimitiveType


}
