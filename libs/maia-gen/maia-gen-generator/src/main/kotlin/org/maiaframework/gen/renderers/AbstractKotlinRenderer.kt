package org.maiaframework.gen.renderers


import org.maiaframework.gen.spec.definition.lang.*
import java.util.Objects


abstract class AbstractKotlinRenderer protected constructor(
    val classDef: ClassDef,
    private val hasSubclasses: Boolean = false
) : AbstractSourceFileRenderer() {


    private val importStatementComparator = Comparator<String> { s1, s2 ->
        if (s1.startsWith("java") && s2.startsWith("java")) {
            s1.compareTo(s2)
        } else if (s1.startsWith("java")) {
            1
        } else if (s2.startsWith("java")) {
            -1
        } else {
            s1.compareTo(s2)
        }

    }


    private val importStatements = sortedSetOf(importStatementComparator)


    private val constructorArgs = mutableListOf<ConstructorArg>()


    override fun renderSource(): String {

        renderGeneratedCodeStatement()
        renderPackageStatement()
        renderImportsPlaceholder()
        renderClassAnnotations()
        renderClassSignature()
        renderClassBody()
        blankLine()

        val sourceWithImportPlaceholder = sourceCode
        val renderedImportStatements = importStatements.joinToString("\n") { "import $it" }

        return sourceWithImportPlaceholder.replace(IMPORTS_PLACEHOLDER.toRegex(), renderedImportStatements)

    }


    override fun renderedFilePath(): String {

        val fqcn = this.classDef.fqcn
        return fqcn.toString().replace(".", "/") + ".kt"

    }


    private fun renderPackageStatement() {

        blankLine()
        appendLine("package ${this.classDef.packageName}")

    }


    /**
     * We don't know what all the imports will be until after the subclass has rendered the methods in the class
     * body. So we just insert a placeholder for now and it will be replaced later.
     */
    private fun renderImportsPlaceholder() {

        blankLine()
        appendLine(IMPORTS_PLACEHOLDER)

    }


    private fun renderClassAnnotations() {

        blankLine()
        blankLine()

        this.classDef.classAnnotations.forEach { annotationDef ->
            addImportFor(annotationDef)
            appendLine(annotationDef.toStringInKotlin())
        }

    }


    private fun renderClassSignature() {

        append(this.classDef.classVisibility.kotlinKeyword)

        if (this.classDef.isAbstract) {
            append("abstract ")
        } else if (hasSubclasses) {
            append("open ")
        }

        append(this.classDef.classType.kotlinText)
        append(" ")
        append(this.classDef.uqcn)

        renderConstructor()

        this.classDef.superclassDef?.let { superclassDef ->

            addImportFor(superclassDef.fqcn)

            append(" : ${superclassDef.nonPrimitiveType.unqualifiedToString}")

            renderCallToSuperConstructor(superclassDef)

        }

        val implementedInterfacesText = this.classDef.interfacesImplemented
            .asSequence()
            .onEach { this.addImportFor(it) }
            .map { it.unqualifiedToString }
            .joinToString(", ")

        if (implementedInterfacesText.isEmpty() == false) {
            append(" : ")
            append(implementedInterfacesText)
        }

    }


    protected open fun renderClassBody() {

        appendLine(" {")
        renderPreClassFields()
        renderInitBlock()
        renderFunctions()
        renderInnerClasses()
        blankLine()
        blankLine()
        renderCompanionObject()
        appendLine("}")

    }


    // To be overridden by subclasses that need to render anything before the class fields are rendered
    protected open fun renderPreClassFields() {

        // Do nothing

    }


    protected open fun renderInitBlock() {

        // do nothing

    }


    private fun notAnInheritedField(constructorArg: ConstructorArg): Boolean {

        return this.classDef.isFieldFromSuperclass(constructorArg.classFieldDef) == false

    }


    private fun anInheritedField(constructorArg: ConstructorArg): Boolean {

        return this.classDef.isFieldFromSuperclass(constructorArg.classFieldDef)

    }


    protected open fun renderConstructor() {

        if (this.constructorArgs.isEmpty()) {
            return
        }

        if (
            this.classDef.classType === ClassType.CLASS
            || this.classDef.classType === ClassType.DATA_CLASS
            || this.classDef.classType === ClassType.ENUM
        ) {

            renderConstructorAnnotations()

            append("(")
            newLine()
            renderConstructorArgs(this.constructorArgs)
            append(")")

        }

    }

    /**
     * Subclasses may override to render any custom code in the body of the constructor after all the constructor
     * arguments have been assigned.
     */
    protected open fun renderAdditionalConstructorBody() {
        // Subclasses may override
    }


    private fun renderConstructorAnnotations() {

        val annotationCount = this.classDef.constructorAnnotations.size

        if (annotationCount == 1) {

            val annotationDef = this.classDef.constructorAnnotations[0]

            newLine()
            addImportFor(annotationDef)
            append("@${annotationDef.unqualifiedToString} constructor")

        } else if (annotationCount > 1) {

            newLine()

            this.classDef.constructorAnnotations.forEach { annotationDef ->

                addImportFor(annotationDef)
                appendLine("@${annotationDef.unqualifiedToString}")

            }

            append(" constructor")

        }

    }


    protected open fun renderConstructorArgs(args: List<ConstructorArg>) {

        val argCount = args.size

        args.forEachIndexed { index, constructorArg ->

            val commaOrNot = if (index + 1 == argCount) "" else ","

            val annotationString = constructorArg.annotationDefs.map { it.toStringInKotlin() + " " }.joinToString("")
            val classField = constructorArg.classFieldDef
            val modifiers = modifiersFor(constructorArg)
            appendLine("    $annotationString$modifiers${classField.classFieldName}: ${classField.unqualifiedToString}$commaOrNot")

        }

    }


    private fun modifiersFor(constructorArg: ConstructorArg): String {

        if (anInheritedField(constructorArg)) {
            return ""

        }

        if (constructorArg.classFieldDef.isConstructorOnly) {
            return ""
        }

        val varOrVal = "val " // TODO will we even need to make fields vars? -  if (constructorArg.classFieldDef.isModifiable) "var " else "val "
        val visibilityModifier = if (constructorArg.classFieldDef.isPrivateProperty) "private " else ""

        return "$visibilityModifier$varOrVal"

    }


    protected open fun renderCallToSuperConstructor(superclassDef: ClassDef) {

        val separator: String

        append("(")

        val inheritedFields = superclassDef.allFieldsSorted

        if (inheritedFields.size > 1) {
            newLine()
            append("    ")
            separator = ",\n    "
        } else {
            separator = ", "
        }

        val textToRender = inheritedFields
            .asSequence()
            .map { fieldDef -> fieldDef.classFieldName.value }
            .joinToString(separator)

        append(textToRender)
        newLine()
        append(")")

    }


    /**
     * Subclasses may override this if they need to render any methods in the class body.
     */
    protected open fun renderFunctions() {
        // do nothing
    }


    /**
     * Subclasses may override this if they need to render any static classes in the class body.
     */
    protected open fun renderInnerClasses() {
        // do nothing
    }


    protected open fun renderCompanionObject() {
        // do nothing
    }


    protected fun addConstructorArg(classFieldDef: ClassFieldDef) {

        addImportFor(classFieldDef.fieldType)
        this.constructorArgs.add(ConstructorArg(classFieldDef))
        this.constructorArgs.sort()

    }


    protected fun addConstructorArg(constructorArg: ConstructorArg) {

        addImportFor(constructorArg)
        this.constructorArgs.add(constructorArg)
        this.constructorArgs.sort()

    }


    protected fun append(uqcn: Uqcn) {

        super.append(uqcn.toString())

    }


    inline fun <reified T> addImportFor(): Unit = addImportFor(T::class.java)


    fun addImportFor(clazz: Class<*>) {

        addImportFor(clazz.canonicalName)

    }


    fun addImportFor(rawFqcn: String) {

        addImportFor(Fqcn.valueOf(rawFqcn))

    }


    fun addImportFor(fqcn: Fqcn) {

        if (fqcn.isInLangPackage == false && isNotImportedByKotlin(fqcn) && fqcn.notInSamePackageAs(this.classDef.fqcn)) {
            this.importStatements.add("$fqcn")
        }

    }


    fun addImportRaw(importStatement: String) {

        this.importStatements.add(importStatement)

    }


    private fun isNotImportedByKotlin(fqcn: Fqcn): Boolean {

        return importedByKotlin.contains(fqcn.toString()) == false

    }


    fun addImportFor(fieldType: FieldType) {

        when (fieldType) {
            is BooleanFieldType -> addImportFor(fieldType.fqcn)
            is BooleanTypeFieldType -> addImportFor(fieldType.fqcn)
            is BooleanValueClassFieldType -> addImportFor(fieldType.fqcn)
            is DataClassFieldType -> addImportFor(fieldType.fqcn)
            is DomainIdFieldType -> addImportFor(fieldType.fqcn)
            is DoubleFieldType -> addImportFor(fieldType.fqcn)
            is EnumFieldType -> addImportFor(fieldType.fqcn)
            is EsDocFieldType -> addImportFor(fieldType.fqcn)
            is ForeignKeyFieldType -> addImportFor(fieldType.fqcn)
            is FqcnFieldType -> addImportFor(fieldType.fqcn)
            is IdAndNameFieldType -> addImportFor(fieldType.fqcn)
            is InstantFieldType -> addImportFor(fieldType.fqcn)
            is IntFieldType -> addImportFor(fieldType.fqcn)
            is IntTypeFieldType -> addImportFor(fieldType.fqcn)
            is IntValueClassFieldType -> addImportFor(fieldType.fqcn)
            is ListFieldType -> {
                addImportFor(fieldType.fqcn)
                addImportFor(fieldType.parameterFieldType)
            }

            is LocalDateFieldType -> addImportFor(fieldType.fqcn)
            is LongFieldType -> addImportFor(fieldType.fqcn)
            is LongTypeFieldType -> addImportFor(fieldType.fqcn)
            is MapFieldType -> {
                addImportFor(fieldType.fqcn)
                addImportFor(fieldType.keyFieldType)
                addImportFor(fieldType.valueFieldType)
            }

            is ObjectIdFieldType -> addImportFor(fieldType.fqcn)
            is PeriodFieldType -> addImportFor(fieldType.fqcn)
            is RequestDtoFieldType -> addImportFor(fieldType.fqcn)
            is SetFieldType -> {
                addImportFor(fieldType.fqcn)
                addImportFor(fieldType.parameterFieldType)
            }

            is SimpleResponseDtoFieldType -> addImportFor(fieldType.fqcn)
            is StringFieldType -> addImportFor(fieldType.fqcn)
            is StringTypeFieldType -> addImportFor(fieldType.fqcn)
            is StringValueClassFieldType -> addImportFor(fieldType.fqcn)
            is UrlFieldType -> addImportFor(fieldType.fqcn)
        }

    }


    fun addImportFor(constructorArg: ConstructorArg) {

        addImportFor(constructorArg.classFieldDef.fqcn)
        constructorArg.annotationDefs.forEach { this.addImportFor(it) }

    }


    fun addImportFor(nonPrimitiveType: NonPrimitiveType) {

        addImportFor(nonPrimitiveType.fqcn)

        if (nonPrimitiveType is ParameterizedType) {
            nonPrimitiveType.parameters.forEach { this.addImportFor(it) }
        }

    }


    fun addImportFor(annotationDef: AnnotationDef) {

        addImportFor(annotationDef.fqcn)

    }


    protected fun fieldNamesAnded(fieldDefs: List<ClassFieldDef>): String {

        return fieldDefs
            .map { fieldDef -> fieldDef.classFieldName.firstToUpper() }
            .joinToString("And")

    }


    protected fun buildFunctionParametersFrom(fieldDefs: List<ClassFieldDef>): String {

        return fieldDefs.joinToString(", ") { fieldDef -> "${fieldDef.classFieldName}: ${fieldDef.unqualifiedToString}" }

    }


    protected fun buildFunctionParameters(fieldDefs: List<ClassFieldDef>): List<String> {

        return fieldDefs
            .map { fieldDef ->
                addImportFor(fieldDef.fieldType)
                "${fieldDef.classFieldName}: ${fieldDef.fqcn.uqcn}"
            }

    }


    protected fun buildMethodParametersWithUnwrappedOptionalsFrom(classFieldDefs: List<ClassFieldDef>): String {

        return classFieldDefs
            .map { fieldDef -> "${fieldDef.classFieldName}: ${fieldDef.unqualifiedToString}" }
            .joinToString(", ")

    }


    protected fun renderEqualsAndHashCode(fieldDefs: List<ClassFieldDef>) {

        addImportFor(Objects::class.java)

        blankLine()
        blankLine()
        appendLine("    @Override")
        appendLine("    public boolean equals(final Object other) {")
        blankLine()
        appendLine("        if (other == this) {")
        appendLine("            return true;")
        appendLine("        }")
        blankLine()
        appendLine("        if (other == null) {")
        appendLine("            return false;")
        appendLine("        }")
        blankLine()
        appendLine("        if (this.getClass() != other.getClass()) {")
        appendLine("            return false;")
        appendLine("        }")
        blankLine()
        appendLine("        final ${classDef.uqcn} that = (${classDef.uqcn}) other;")
        blankLine()
        appendLine("        return ")

        appendLines(
            fieldDefs.map { fd -> { append("            Objects.equals(this.${fd.getterMethodName}(), that.${fd.getterMethodName}())") } },
            { appendLine(" &&") },
            { appendLine(";") })

        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    @Override")
        appendLine("    public int hashCode() {")
        blankLine()
        appendLine("        return Objects.hash(")

        appendLines(
            fieldDefs.map { fd -> { append("            ${fd.getterMethodName}()") } },
            { appendLine(",") },
            { appendLine(");") }
        )

        blankLine()
        appendLine("    }")

    }


    protected fun appendLines(lines: List<() -> Unit>, separator: () -> Unit, finalizer: () -> Unit) {

        appendLines(lines, separator)
        finalizer()

    }


    protected fun appendLines(lines: List<() -> Unit>, separator: () -> Unit) {

        lines.reduce { r1, r2 ->
            {
                r1()
                separator()
                r2()
            }
        }.invoke()

    }


    protected fun getConstructorArgs(): List<ConstructorArg> {

        return this.constructorArgs

    }


    companion object {

        private const val IMPORTS_PLACEHOLDER = "IMPORTS_PLACEHOLDER"

        private val importedByKotlin = setOf(
            "java.util.List",
            "java.util.Set",
            "java.util.Map"
        )

    }


}
