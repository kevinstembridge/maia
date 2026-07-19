package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.HasSingleEffectiveRecord


data class EffectiveRangeDef(
    val managedBy: EffectiveRangeManagedBy,
    val dateType: EffectiveRangeDateType,
    val hasSingleEffectiveRecord: HasSingleEffectiveRecord = HasSingleEffectiveRecord.FALSE
)
