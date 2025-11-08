package org.maiaframework.gen.spec.definition

@JvmInline
value class ModuleName private constructor(val value: String) {


    companion object {


        fun of(value: String): ModuleName {

            require(value.isNotBlank()) { "value must not be blank"}
            return ModuleName(value)

        }


    }


}
