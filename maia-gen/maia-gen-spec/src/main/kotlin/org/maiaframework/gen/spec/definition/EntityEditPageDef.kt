package org.maiaframework.gen.spec.definition

class EntityEditPageDef(
    val entityDef: EntityDef,
    val pageTitle: String,
    val authority: AuthorityDef?
) {


    private val entityEditBaseName = entityDef.entityBaseName.withSuffix("EntityEdit")


    val editFormAngularComponentNames = AngularComponentNames(
        entityDef.packageName,
        entityEditBaseName.withSuffix("Form").value
    )


    val editFormPageAngularComponentNames = AngularComponentNames(
        entityDef.packageName,
        entityEditBaseName.withSuffix("Page").value
    )


    val dataPageId = "${entityDef.entityBaseName.toSnakeCase()}_edit"


    val viewPageUrl: String get() = entityDef.viewEntityUrl


    val updateApiDef: EntityUpdateApiDef
        get() = entityDef.entityCrudApiDef?.updateApiDef
            ?: error("Entity ${entityDef.entityBaseName} has no updateApiDef — cannot create EntityEditPageDef")


}
