package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.EffectiveRangeManagedBy
import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
import org.maiaframework.gen.spec.definition.EffectiveRangeDef


@MaiaDslMarker
class ManyToManyEntityDefBuilder {


    internal var effectiveRangeDef: EffectiveRangeDef? = null


    internal var description: Description? = null


    fun effectiveRange(
        managedBy: EffectiveRangeManagedBy = EffectiveRangeManagedBy.SYSTEM,
        dateType: EffectiveRangeDateType = EffectiveRangeDateType.TIMESTAMP
    ) {

        effectiveRangeDef = EffectiveRangeDef(managedBy, dateType)

    }


    fun description(description: String) {

        this.description = Description(description)

    }


}
