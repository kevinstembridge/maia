package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.flags.TextCase
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import org.maiaframework.gen.spec.definition.validation.LengthConstraintDef
import java.util.SortedSet


class OtherFieldDefBuilder {


    var displayName: String? = null


    var description: String? = null


    var formPlaceholderText: String? = null


    var minLength: Long? = null


    var maxLength: Long? = null


    var textCase: TextCase = TextCase.ORIGINAL


    fun buildValidationConstraints(): SortedSet<AbstractValidationConstraintDef> {

        val set = mutableSetOf<AbstractValidationConstraintDef>()

        if (minLength != null || maxLength != null) {
            set.add(LengthConstraintDef.of(minLength, maxLength))
        }

        return set.toSortedSet()

    }


}
