package org.maiaframework.gen.spec.definition


class EnumValueDef(
    val name: String,
    val description: Description? = null,
    val displayName: DisplayName? = null,
) {


    init {

        require(name.isNotBlank()) { "Name must not be blank" }

    }


    val displayNameNonNull: DisplayName
        get() = this.displayName ?: throw IllegalStateException("Expecting enum value $name to have a display name.")


}
