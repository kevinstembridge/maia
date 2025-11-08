package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityDetailDtoDef
import org.maiaframework.gen.spec.definition.lang.IdAndNameFieldType

class EntityDetailDtoHtmlRenderer(private val entityDetailDtoDef: EntityDetailDtoDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return entityDetailDtoDef.componentBaseName.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        appendLine("@if (detailDto$ | async; as detailDto) {")
        appendLine("  <div>")

        this.entityDetailDtoDef.dtoDef.allFields.forEach { classFieldDef ->

            val pipes = if (classFieldDef.pipes.isEmpty()) {
                ""
            } else {
                classFieldDef.pipes.joinToString(prefix = " | ", separator = " | ")
            }

            val fieldType = classFieldDef.fieldType

            if (fieldType is IdAndNameFieldType) {

                append("""
                    |    <div class="row">
                    |      <div class="col">${classFieldDef.displayName}</div>
                    |      <div class="col">{{detailDto.${classFieldDef.classFieldName}.name$pipes}}</div>
                    |    </div>
                    |""".trimMargin()
                )

            } else {

                append("""
                    |    <div class="row">
                    |      <div class="col">${classFieldDef.displayName}</div>
                    |      <div class="col">{{detailDto.${classFieldDef.classFieldName}$pipes}}</div>
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
