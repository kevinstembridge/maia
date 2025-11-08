package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


class RefreshTypeaheadIndexJobRenderer(private val typeaheadDef: TypeaheadDef) : AbstractKotlinRenderer(typeaheadDef.refreshIndexJobClassDef) {


    init {

        addConstructorArg(ClassFieldDef.aClassField("service", this.typeaheadDef.indexServiceClassDef.fqcn).privat().build())

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.MAHANA_JOB_NAME)

        blankLine()
        appendLine("    override val jobName = JobName(\"refresh${this.typeaheadDef.typeaheadName}TypeaheadIndex\")")
        blankLine()
        appendLine("    override val description = \"Refresh the Elastic Search index for ${this.typeaheadDef.typeaheadName} typeahead records.\"")

    }

    override fun renderFunctions() {

        renderMethod_executeJob()

    }


    private fun renderMethod_executeJob() {

        addImportFor(Fqcns.MAHANA_JOB_METRICS)

        blankLine()
        blankLine()
        appendLine("    override fun executeJob(jm: JobMetrics) {")
        blankLine()
        appendLine("        this.service.refreshIndex(jm)")
        blankLine()
        appendLine("    }")

    }


}
