package org.maiaframework.gen.spec.definition.lang

class EnumType(fqcn: Fqcn) : NonPrimitiveType(fqcn, fqcn.uqcn.toString()) {


    override val isEnum = true


    override val isSetOfEnums = false


    override val isString = false


    override val simpleTypeUnderlyingFieldType: FieldType? = null


    override fun asParameterTo(fqcn: Fqcn): ParameterizedType {

        return ParameterizedType(fqcn, null, this)

    }


    override fun withPrefix(prefix: String): EnumType {

        return EnumType(fqcn.withPrefix(prefix))

    }


}
