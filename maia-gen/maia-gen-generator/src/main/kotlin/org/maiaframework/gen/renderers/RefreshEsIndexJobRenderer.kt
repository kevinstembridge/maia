package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


class RefreshEsIndexJobRenderer(private val esDocDef: EsDocDef) : AbstractKotlinRenderer(esDocDef.refreshIndexJobClassDef) {


    init {

        addConstructorArg(ClassFieldDef.aClassField("service", this.esDocDef.indexServiceClassDef.fqcn).privat().build())

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.MAIA_JOB_NAME)

        blankLine()
        blankLine()
        appendLine("    override val jobName = JobName(\"${this.esDocDef.refreshEsIndexJobName}\")")
        blankLine()
        blankLine()
        appendLine("    override val description = \"Refresh the Elastic Search index for ${this.esDocDef.esDocBaseName} records. Index description: ${this.esDocDef.indexDescription}\"")

    }

    override fun renderFunctions() {

        renderMethod_executeJob()

    }


    private fun renderMethod_executeJob() {

        addImportFor(Fqcns.MAIA_JOB_METRICS)
        addImportRaw("kotlinx.coroutines.runBlocking")

        blankLine()
        blankLine()
        appendLine("    override fun executeJob(jm: JobMetrics) {")
        blankLine()
        appendLine("        runBlocking {")
        appendLine("            service.refreshIndex(jm)")
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


}
