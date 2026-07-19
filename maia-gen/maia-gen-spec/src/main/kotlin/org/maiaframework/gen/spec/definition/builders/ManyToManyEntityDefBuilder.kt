package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EffectiveRangeManagedBy
import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
import org.maiaframework.gen.spec.definition.EffectiveRangeDef
import org.maiaframework.gen.spec.definition.ModuleName
import org.maiaframework.gen.spec.definition.flags.HasSingleEffectiveRecord


@MaiaDslMarker
class ManyToManyEntityDefBuilder {


    internal var effectiveRangeDef: EffectiveRangeDef? = null


    internal var description: Description? = null


    internal var moduleName: ModuleName? = null


    fun effectiveRange(
        managedBy: EffectiveRangeManagedBy = EffectiveRangeManagedBy.SYSTEM,
        dateType: EffectiveRangeDateType = EffectiveRangeDateType.TIMESTAMP,
        hasSingleEffectiveRecord: HasSingleEffectiveRecord = HasSingleEffectiveRecord.FALSE,
    ) {

        this.effectiveRangeDef = EffectiveRangeDef(managedBy, dateType, hasSingleEffectiveRecord)

    }


    fun description(description: String) {

        this.description = Description(description)

    }


    fun moduleName(moduleName: String) {

        this.moduleName = ModuleName.of(moduleName)

    }


}
