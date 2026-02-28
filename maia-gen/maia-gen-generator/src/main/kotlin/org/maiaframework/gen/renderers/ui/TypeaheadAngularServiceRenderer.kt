package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.TypeaheadDef

class TypeaheadAngularServiceRenderer(private val typeaheadDef: TypeaheadDef) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpParams")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("rxjs", "Observable")
        addImport("rxjs", "of")
        addImport("rxjs/operators", "catchError")
        addImport(typeaheadDef.esDocDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return typeaheadDef.typescriptServiceRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
            |
            |
            |@Injectable()
            |export class ${this.typeaheadDef.angularServiceClassName} {
            |
            |
            |    private readonly http = inject(HttpClient);
            |
            |
            |    search(term: string): Observable<${this.typeaheadDef.esDocDef.uqcn}[]> {
            |
            |        if (typeof term !== 'string') {
            |            return of([]);
            |        }
            |
            |        term = term.trim();
            |
            |        const options = term ? { params: new HttpParams().set('q', term) } : {};
            |
            |        return this.http.get<${this.typeaheadDef.esDocDef.uqcn}[]>('${this.typeaheadDef.endpointUrl}', options).pipe(
            |            catchError(this.handleError<${this.typeaheadDef.esDocDef.uqcn}[]>('search', []))
            |        );
            |
            |    }
            |
            |
            |    private handleError<T>(operation = 'operation', result?: T) {
            |
            |        return (error: any): Observable<T> => {
            |
            |            // TODO: send the error to remote logging infrastructure
            |            console.error(error);
            |
            |            return of(result as T);
            |
            |        };
            |
            |    }
            |
            |
            |}""".trimMargin())

    }


}
