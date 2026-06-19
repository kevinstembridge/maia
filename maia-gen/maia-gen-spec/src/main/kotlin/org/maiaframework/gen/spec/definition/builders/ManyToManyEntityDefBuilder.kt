package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.EffectiveRangeManagedBy
import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
import org.maiaframework.gen.spec.definition.EffectiveRangeDef


@MaiaDslMarker
class ManyToManyEntityDefBuilder {


    internal var effectiveRangeDef: EffectiveRangeDef? = null


    fun effectiveRange(
        managedBy: EffectiveRangeManagedBy = EffectiveRangeManagedBy.SYSTEM,
        dateType: EffectiveRangeDateType = EffectiveRangeDateType.TIMESTAMP
    ) {

        effectiveRangeDef = EffectiveRangeDef(managedBy, dateType)

    }


}
