package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.ResponseDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ResponseDtoEndpointRenderer(private val responseDtoDef: ResponseDtoDef) : AbstractKotlinRenderer(responseDtoDef.endpointClassDef) {


    private val dtoUqcn = this.responseDtoDef.dtoDef.uqcn
    private val dtoBaseName = this.responseDtoDef.dtoBaseName


    init {

        val repoFqcn = responseDtoDef.repoClassDef.fqcn
        val searchParserFqcn = responseDtoDef.searchParserClassDef.fqcn

        addConstructorArg(ClassFieldDef.aClassField("repo", repoFqcn).build())
        addConstructorArg(ClassFieldDef.aClassField("searchParser", searchParserFqcn).build())

    }


    override fun renderPreClassFields() {

        addImportFor(DateTimeFormatter::class.java)
        addImportFor(ZoneId::class.java)

        blankLine()
        appendLine("    private val fileTimestampFormatter = DateTimeFormatter.ofPattern(\"yyyy-MM-dd-HHmm\").withZone(ZoneId.of(\"UTC\"))")
        blankLine()
        appendLine("    private val csvHelper = ${this.responseDtoDef.csvHelperClassDef.uqcn}()")


    }

    override fun renderFunctions() {

        renderMethod_search()
        renderMethod_export()

    }


    private fun renderMethod_search() {

        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_PAGE)
        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)
        addImportFor(Fqcns.SPRING_RESPONSE_BODY)
        addImportFor(Fqcns.MAIA_MONGO_PAGEABLE_SEARCH_REQUEST)

        blankLine()
        blankLine()
        appendLine("    @PostMapping(path = [\"/api/search/${this.dtoBaseName.toSnakeCase()}\"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])")
        appendPreAuthorize()
        appendLine("    @ResponseBody")
        appendLine("    fun search(@RequestBody rawSearchJson: String): Page<${this.dtoUqcn}>  {")
        blankLine()
        appendLine("        val searchRequest: MongoPageableSearchRequest = this.searchParser.parseSearchRequest(rawSearchJson)")
        appendLine("        return this.repo.search(searchRequest)")
        blankLine()
        appendLine("    }")

    }


    private fun renderMethod_export() {

        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_HTTP_HEADERS)
        addImportFor(Fqcns.SPRING_HTTP_STATUS)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)
        addImportFor(Fqcns.SPRING_RESPONSE_ENTITY)
        addImportFor(Fqcns.SPRING_STREAMING_RESPONSE_BODY)
        addImportFor(Fqcns.CSV_STREAMING_RESPONSE_BODY)
        addImportFor(Instant::class.java)

        blankLine()
        blankLine()
        appendLine("    @PostMapping(path = [\"/api/export/${this.dtoBaseName.toSnakeCase()}\"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [\"text/csv\"])")
        appendPreAuthorize()
        appendLine("    fun export(@RequestBody rawSearchJson: String): ResponseEntity<StreamingResponseBody> {")
        blankLine()
        appendLine("        val searchRequest: MongoPageableSearchRequest = this.searchParser.parseSearchRequest(rawSearchJson)")
        appendLine("        val dtos: List<${this.dtoUqcn}> = this.repo.search(searchRequest).content")
        blankLine()
        appendLine("        val streamingResponseBody: CsvStreamingResponseBody<${this.dtoUqcn}> = CsvStreamingResponseBody(this.csvHelper, dtos.stream())")
        blankLine()
        appendLine("        val timestamp = fileTimestampFormatter.format(Instant.now())")
        appendLine("        val fileName = \"${this.dtoBaseName.toSnakeCase()}_\$timestamp.csv\"")
        blankLine()
        appendLine("        val headers = HttpHeaders()")
        appendLine("        headers.add(\"Content-Disposition\", \"attachment; filename=\\\"\$fileName\\\"\")")
        appendLine("        headers.add(\"Content-Type\", \"text/csv;charset=utf-8\")")
        blankLine()
        appendLine("        return ResponseEntity(streamingResponseBody, headers, HttpStatus.OK)")
        blankLine()
        appendLine("    }")

    }


    private fun appendPreAuthorize() {

        this.responseDtoDef.withPreAuthorize.let { expression ->
            addImportFor(Fqcns.SPRING_SECURITY_PRE_AUTHORIZE)
            appendLine("    @PreAuthorize(\"$expression\")")
        }

    }


}
