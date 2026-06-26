package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType

class EntityDetailViewContentHtmlRenderer(private val entityDetailViewDef: EntityDetailViewDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return entityDetailViewDef.viewContentComponentHtmlRenderedFilePath

    }


    override fun renderSource(): String {

        appendLine("@if (detailDto$ | async; as detailDto) {")
        appendLine("  <div>")

        this.entityDetailViewDef.dtoDef.allFields.forEach { classFieldDef ->

            val pipes = if (classFieldDef.pipes.isEmpty()) {
                ""
            } else {
                classFieldDef.pipes.joinToString(prefix = " | ", separator = " | ")
            }

            val fieldType = classFieldDef.fieldType

            if (fieldType is PkAndNameFieldType) {

                append("""
                    |    <div class="detail-row">
                    |      <div class="detail-label">${classFieldDef.displayName}</div>
                    |      <div class="detail-value">{{detailDto.${classFieldDef.classFieldName}.name$pipes}}</div>
                    |    </div>
                    |""".trimMargin()
                )

            } else if (fieldType is ListFieldType && fieldType.parameterFieldType is PkAndNameFieldType) {

                append("""
                    |    <div class="detail-row">
                    |      <div class="detail-label">${classFieldDef.displayName}</div>
                    |      <div class="detail-value">
                    |        @for (item of detailDto.${classFieldDef.classFieldName}; track item.id) {<span>{{item.name}}</span>}
                    |      </div>
                    |    </div>
                    |""".trimMargin()
                )

            } else {

                append("""
                    |    <div class="detail-row">
                    |      <div class="detail-label">${classFieldDef.displayName}</div>
                    |      <div class="detail-value">{{detailDto.${classFieldDef.classFieldName}$pipes}}</div>
                    |    </div>
                    |""".trimMargin()
                )

            }

        }

        appendLine("  </div>")
        appendLine("} @else {")
        appendLine("  <mat-spinner></mat-spinner>")
        appendLine("}")

        return sourceCode.toString()

    }


}
