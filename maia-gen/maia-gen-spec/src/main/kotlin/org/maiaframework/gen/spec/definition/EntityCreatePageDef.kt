package org.maiaframework.gen.spec.definition

class EntityCreatePageDef(
    val entityDef: EntityDef,
    val pageTitle: String,
    val authority: AuthorityDef?
) {


    private val entityCreateBaseName = entityDef.entityBaseName.withSuffix("EntityCreate")


    val createFormAngularComponentNames = AngularComponentNames(
        entityDef.packageName,
        entityCreateBaseName.withSuffix("Form").value
    )


    val createPageAngularComponentNames = AngularComponentNames(
        entityDef.packageName,
        entityCreateBaseName.withSuffix("Page").value
    )


    val dataPageId = "${entityDef.entityBaseName.toSnakeCase()}_create"


    val createPageUrl: String get() = entityDef.createEntityPageUrl


    val createApiDef: EntityCreateApiDef
        get() = entityDef.entityCrudApiDef?.createApiDef
            ?: error("Entity ${entityDef.entityBaseName} has no createApiDef — cannot create EntityCreatePageDef")


}
