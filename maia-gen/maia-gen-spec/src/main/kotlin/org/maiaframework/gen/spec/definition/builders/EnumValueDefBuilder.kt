package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.DisplayName
import org.maiaframework.gen.spec.definition.EnumValueDef


@MaiaDslMarker
class EnumValueDefBuilder(private val name: String) {


    var description: String? = null


    var displayName: String? = null


    var isDefaultFormValue: Boolean = false


    fun build(): EnumValueDef {

        return EnumValueDef(name, description(), displayName(), isDefaultFormValue)

    }


    private fun description(): Description? {

        return this.description?.let { Description(it) }

    }


    private fun displayName(): DisplayName? {

        return this.displayName?.let { DisplayName(it) }

    }


}
