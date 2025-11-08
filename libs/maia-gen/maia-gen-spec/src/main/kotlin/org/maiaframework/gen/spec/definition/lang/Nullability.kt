package org.maiaframework.gen.spec.definition.lang

enum class Nullability(val nullable: Boolean) {

    NULLABLE(true),
    NOT_NULLABLE(false);


    companion object {


        fun of(nullable: Boolean): Nullability {

            return if (nullable) {
                NULLABLE
            } else {
                NOT_NULLABLE
            }

        }


    }

}
