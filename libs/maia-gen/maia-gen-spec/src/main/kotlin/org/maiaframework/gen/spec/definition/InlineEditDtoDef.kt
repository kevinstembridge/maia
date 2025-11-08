package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.Uqcn


class InlineEditDtoDef(val requestDtoDef: RequestDtoDef, val fieldDef: EntityFieldDef) {

    val uqcn: Uqcn = this.requestDtoDef.uqcn

    val fqcn: Fqcn = this.requestDtoDef.fqcn

    val preAuthorizeExpression = this.requestDtoDef.preAuthorizeExpression

}
