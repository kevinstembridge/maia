package org.maiaframework.gen.spec.definition

import org.maiaframework.types.StringType

class ActionName(value: String): StringType<ActionName>(value) {

    companion object {

        val delete = ActionName("delete")
        val edit = ActionName("edit")

    }

}
