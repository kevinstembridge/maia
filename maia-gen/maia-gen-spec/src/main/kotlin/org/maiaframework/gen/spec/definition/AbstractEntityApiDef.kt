package org.maiaframework.gen.spec.definition

abstract class AbstractEntityApiDef(val entityDef: EntityDef) {


    abstract val angularDialogComponentNames: AngularComponentNames


    abstract val angularFormComponentNames: AngularComponentNames


    abstract val requestDtoDef: RequestDtoDef


}
