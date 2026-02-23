package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.TypescriptImport

object TypescriptImports {


    val currentUserStore = TypescriptImport("CurrentUserStore", "@maia-platform/maia-application-common/src/stores/current-user.store")


    val problemDetail = TypescriptImport("ProblemDetail", "@maia/maia-ui")


}
