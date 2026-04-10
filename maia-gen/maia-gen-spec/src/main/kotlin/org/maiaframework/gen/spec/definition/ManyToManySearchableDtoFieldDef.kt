package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


class ManyToManySearchableDtoFieldDef(
    classFieldDef: ClassFieldDef,
    sortIndexAndDirection: SortIndexAndDirection?
) : AbstractSearchableDtoFieldDef(
    classFieldDef,
    sortIndexAndDirection
) {

    override fun copyWithFieldName(dtoFieldName: String): AbstractSearchableDtoFieldDef {

        return ManyToManySearchableDtoFieldDef(
            classFieldDef.withFieldName(dtoFieldName),
            sortIndexAndDirection
        )

    }


}
