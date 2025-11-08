package org.maiaframework.gen.spec.definition

data class ConfigurableSchemaPropertyName(
    val propertyName: String,
    val defaultValue: String?
) {


    override fun toString(): String {

        val defaultSuffix = if (defaultValue == null) {
            ""
        } else {
            ":$defaultValue"
        }

        return "$propertyName$defaultSuffix"

    }

}
