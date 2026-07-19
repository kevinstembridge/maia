package org.maiaframework.gen.spec.definition.jdbc

import org.maiaframework.gen.spec.definition.ModelDefinitionException

object PostgresIdentifiers {


    const val MAX_LENGTH = 63


    /**
     * Postgres silently truncates identifiers over [MAX_LENGTH] bytes rather than rejecting them,
     * which can make two distinct generated names collide once truncated. Fail generation instead,
     * so the spec author can supply a shorter identifier explicitly.
     */
    fun requireValidLength(identifier: String, kind: String, guidance: String) {

        if (identifier.length > MAX_LENGTH) {
            throw ModelDefinitionException(
                "Generated $kind '$identifier' is ${identifier.length} characters, exceeding PostgreSQL's $MAX_LENGTH character identifier limit. $guidance"
            )
        }

    }


}
