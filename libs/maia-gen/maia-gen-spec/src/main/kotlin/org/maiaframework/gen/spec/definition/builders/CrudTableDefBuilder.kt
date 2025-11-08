package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.CrudTableDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableDef
import org.maiaframework.gen.spec.definition.EntityCrudApiDef

class CrudTableDefBuilder(private val dtoDef: DtoHtmlTableDef, private val entityCrudApiDef: EntityCrudApiDef) {


    fun build(): CrudTableDef {

        return CrudTableDef(dtoDef, entityCrudApiDef)

    }


}
