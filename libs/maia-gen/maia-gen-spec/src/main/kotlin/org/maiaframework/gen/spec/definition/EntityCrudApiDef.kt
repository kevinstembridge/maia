package org.maiaframework.gen.spec.definition


class EntityCrudApiDef(
    val entityDef: EntityDef,
    val createApiDef: EntityCreateApiDef?,
    val updateApiDef: EntityUpdateApiDef?,
    val deleteApiDef: EntityDeleteApiDef?,
    val superclassCrudApiDef: EntityCrudApiDef?
) {


    val isEmpty = this.createApiDef == null && this.updateApiDef == null && this.deleteApiDef == null


    val angularComponentNames = entityDef.crudAngularComponentNames


}
