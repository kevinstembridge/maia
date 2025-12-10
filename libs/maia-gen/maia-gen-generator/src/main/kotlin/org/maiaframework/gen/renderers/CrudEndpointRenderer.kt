package org.maiaframework.gen.renderers

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.spec.definition.CrudApiDef
import org.maiaframework.gen.spec.definition.EntityCrudApiDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.DatabaseIndexDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.InlineEditDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class CrudEndpointRenderer(
    private val entityCrudApiDef: EntityCrudApiDef
) : AbstractKotlinRenderer(
    entityCrudApiDef.entityDef.crudEndpointClassDef
) {


    private val entityDef: EntityDef = entityCrudApiDef.entityDef


    init {

        val crudServiceFqcn = this.entityDef.crudDef.crudApiDefs.customCrudServiceFqcn?.fqcn
            ?: this.entityDef.crudServiceClassDef.fqcn

        addConstructorArg(aClassField("crudService", crudServiceFqcn).build())

        if (this.entityDef.uniqueIndexDefs.any { it.withExistsEndpoint }) {

            val daoFqcn = this.entityDef.daoFqcn
            addConstructorArg(aClassField(daoFqcn.uqcn.firstToLower(), daoFqcn).build())

        }

    }


    override fun renderFunctions() {

        `render function create`()
        `render function update`()
        `render function delete`()
        `render inline endpoints`()
        `render existsBy for unique indexes`()

    }


    private fun `render function create`() {

        val createApiDef = this.entityCrudApiDef.createApiDef ?: return

        addImportFor(Fqcns.JAKARTA_VALIDATION_VALID)
        addImportFor(Fqcns.SPRING_HTTP_STATUS)
        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)
        addImportFor(Fqcns.SPRING_RESPONSE_STATUS)

        val createDtoUqcn = createApiDef.requestDtoDef.uqcn

        blankLine()
        blankLine()
        appendLine("    @PostMapping(\"${createApiDef.endpointUrl}\")")
        appendPreAuthorize(createApiDef.crudApiDef)
        appendLine("    @ResponseStatus(HttpStatus.CREATED)")
        appendLine("    fun create(@RequestBody @Valid createDto: $createDtoUqcn) {")
        blankLine()
        appendLine("        this.crudService.create(createDto)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function update`() {

        val apiDef = this.entityCrudApiDef.updateApiDef
            ?: return

        apiDef.requestDtoDef.let { dtoDef ->

            addImportFor(Fqcns.SPRING_PUT_MAPPING)
            addImportFor(Fqcns.SPRING_REQUEST_BODY)
            addImportFor(Fqcns.JAKARTA_VALIDATION_VALID)
            addImportFor(Fqcns.SPRING_MEDIA_TYPE)

            blankLine()
            blankLine()
            appendLine("    @PutMapping(\"${apiDef.endpointUrl}\", produces = [MediaType.APPLICATION_JSON_VALUE])")
            appendPreAuthorize(apiDef.crudApiDef)
            appendLine("    fun update(@RequestBody @Valid editDto: ${dtoDef.uqcn}) {")
            blankLine()
            appendLine("        this.crudService.update(editDto)")
            blankLine()
            appendLine("    }")

        }

    }


    private fun `render function delete`() {

        val apiDef = this.entityCrudApiDef.deleteApiDef ?: return

        addImportFor(Fqcns.SPRING_DELETE_MAPPING)
        addImportFor(Fqcns.SPRING_PATH_VARIABLE)
        addImportFor<DomainId>()

        blankLine()
        blankLine()
        appendLine("    @DeleteMapping(\"${apiDef.endpointUrl}{id}\")")
        appendPreAuthorize(apiDef.crudApiDef)
        appendLine("    fun deleteById(@PathVariable(\"id\") id: DomainId) {")
        blankLine()
        appendLine("        this.crudService.delete(id)")
        blankLine()
        appendLine("    }")

    }


    private fun `render inline endpoints`() {

        this.entityCrudApiDef.updateApiDef?.let { apiDef ->
            apiDef.inlineEditDtoDefs.forEach { renderInlineEndpoint(it) }
        }

    }


    private fun renderInlineEndpoint(dtoDef: InlineEditDtoDef) {

        addImportFor(Fqcns.JAKARTA_VALIDATION_VALID)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_PUT_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)

        val dtoUqcn = dtoDef.uqcn
        val fieldName = dtoDef.fieldDef.classFieldDef.classFieldName

        blankLine()
        blankLine()
        appendLine("    @PutMapping(\"/api/${this.entityDef.entityBaseName.toSnakeCase()}/inline/${fieldName.toSnakeCase()}\", produces = [MediaType.APPLICATION_JSON_VALUE])")
        appendLine("    fun update${fieldName.firstToUpper()}(@RequestBody @Valid editDto: $dtoUqcn) {")
        blankLine()
        appendLine("        this.crudService.update${fieldName.firstToUpper()}(editDto)")
        blankLine()
        appendLine("    }")

    }


    private fun `render existsBy for unique indexes`() {

        this.entityDef.uniqueIndexDefs.filter { it.withExistsEndpoint }.forEach { renderExistsByForUniqueIndex(it) }

    }


    private fun renderExistsByForUniqueIndex(databaseIndexDef: DatabaseIndexDef) {

        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)
        addImportFor(Fqcns.JAKARTA_VALIDATION_VALID)
        addImportFor(Fqcns.FORM_VALIDATION_RESPONSE_DTO)

        val dtoClassName = databaseIndexDef.asyncValidator.asyncValidationDtoName
        val url = databaseIndexDef.existsByUrl
        val functionName = databaseIndexDef.existsByFunctionName
        val fieldNames = databaseIndexDef.indexDef.classFieldDefs.map { it.classFieldName }

        blankLine()
        blankLine()
        appendLine("    @PostMapping(\"$url\", produces = [MediaType.APPLICATION_JSON_VALUE])")
        appendLine("    fun $functionName(@RequestBody @Valid dto: $dtoClassName): FormValidationResponseDto {")

        fieldNames.forEach { fieldName ->
            blankLine()
            appendLine("        val $fieldName = dto.$fieldName")
        }

        blankLine()
        appendLine("        val invalid = this.${this.entityDef.daoFqcn.uqcn.firstToLower()}.$functionName(${fieldNames.joinToString(", ")})")
        blankLine()
        appendLine("        val message = if (invalid) {")

        if (fieldNames.size == 1) {

            val classFieldDef = databaseIndexDef.indexDef.entityFieldDefs.first().classFieldDef
            val displayName = classFieldDef.displayName
                ?: throw RuntimeException("The '${classFieldDef.classFieldName}' field on database index ${databaseIndexDef.indexDef.indexName} does not have a display name.")

            appendLine("            \"This $displayName is already in use.\"")

        } else {
            appendLine("            \"A record already exists for the fields: ${databaseIndexDef.indexDef.entityFieldDefs.map { it.classFieldDef.displayName!! }.joinToString(", ")}.\"")
        }

        appendLine("        } else {")
        appendLine("            null")
        appendLine("        }")
        blankLine()
        appendLine("        return FormValidationResponseDto(invalid = invalid, message = message)")
        blankLine()
        appendLine("    }")

    }


    private fun appendPreAuthorize(crudApiDef: CrudApiDef) {

        // TODO find out if we can do this in Spring Boot 3
//        crudApiDef.authority?.let { authority ->
//            addImportFor(Fqcns.SPRING_SECURITY_PRE_AUTHORIZE)
//            appendLine("    @PreAuthorize(\"hasAuthority('$authority')\")")
//        }

    }


}
