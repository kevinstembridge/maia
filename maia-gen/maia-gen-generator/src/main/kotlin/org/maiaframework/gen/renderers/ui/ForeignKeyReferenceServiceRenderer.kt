package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.MaiaGenConstants


class ForeignKeyReferenceServiceRenderer(private val entityDefs: Set<EntityDef>) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("rxjs", "Observable")
        addImport(
            "@app/gen-components/common/model/${MaiaGenConstants.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO_CLASS_NAME}",
            MaiaGenConstants.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO_CLASS_NAME
        )

    }


    override fun renderedFilePath(): String {

        return MaiaGenConstants.FOREIGN_KEY_REFERENCE_SERVICE_RENDERED_FILE_PATH

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("@Injectable({providedIn: 'root'})")
        appendLine("export class ${MaiaGenConstants.FOREIGN_KEY_REFERENCE_SERVICE_CLASS_NAME} {")
        blankLine()
        blankLine()
        appendLine("    private httpOptions = {")
        appendLine("        headers: new HttpHeaders({")
        appendLine("            'Content-Type': 'application/json'")
        appendLine("        })")
        appendLine("    };")
        blankLine()
        blankLine()
        appendLine("    private readonly http = inject(HttpClient);")

        this.entityDefs.forEach { entityDef ->

            val entityKey = entityDef.entityBaseName

            blankLine()
            blankLine()
            appendLine("    public check${entityKey}ForReferences(id: string): Observable<${MaiaGenConstants.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO_CLASS_NAME}> {")
            blankLine()
            appendLine("        return this.http.get<${MaiaGenConstants.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO_CLASS_NAME}>(")
            appendLine("            '${entityDef.checkForeignKeyReferencesEndpointUrl}/' + id,")
            appendLine("            this.httpOptions")
            appendLine("        );")
            blankLine()
            appendLine("    }")

        }

        blankLine()
        blankLine()
        appendLine("}")

    }


}
