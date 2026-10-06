package org.maiaframework.gen.renderers.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.AuthoritiesDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File

class EntityCrudRoutesRendererHistoryTest {


    @Test
    fun `history route requires the authority configured for the history blotter`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val readAuthority = authority("WIDGET_READ")

            val writeAuthority = authority("WIDGET_WRITE")

            val widget = entity("com.example", "Widget", recordVersionHistory = true) {
                field("name", FieldTypes.string) { editableByUser() }
                historyBlotter { authority(readAuthority) }
                crud {
                    authority(writeAuthority)
                    update { api {} }
                }
            }

        }

        val routes = renderRoutes(spec, spec.widget, tempDir)

        assertThat(historyRouteLine(routes)).contains("Authority.WIDGET_READ").doesNotContain("WIDGET_WRITE")

    }


    @Test
    fun `history route falls back to the entity authority when the history blotter has none`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val writeAuthority = authority("WIDGET_WRITE")

            val widget = entity("com.example", "Widget", recordVersionHistory = true) {
                field("name", FieldTypes.string) { editableByUser() }
                crud {
                    authority(writeAuthority)
                    update { api {} }
                }
            }

        }

        val routes = renderRoutes(spec, spec.widget, tempDir)

        assertThat(historyRouteLine(routes)).contains("Authority.WIDGET_WRITE")

    }


    @Test
    fun `history route has no authority data when none is configured anywhere`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val widget = entity("com.example", "Widget", recordVersionHistory = true) {
                field("name", FieldTypes.string)
            }

        }

        val routes = renderRoutes(spec, spec.widget, tempDir)

        assertThat(routes).contains("history").doesNotContain("authorities")

    }


    private fun renderRoutes(spec: AbstractSpec, entityDef: EntityDef, tempDir: File): String {

        val modelDef = spec.modelDef

        val applicationSpec = object : ApplicationSpec("com.example") {
            override val modelDefs: List<ModelDef> = listOf(modelDef)
        }

        val authoritiesDef: AuthoritiesDef? = applicationSpec.applicationModelDef.authoritiesDef

        EntityCrudRoutesRenderer(entityDef, authoritiesDef).renderToDir(tempDir)

        return tempDir.walkTopDown().single { it.name.endsWith("-routes.ts") }.readText()

    }


    private fun historyRouteLine(routes: String): String {

        return routes.lines().single { it.contains("authorities") }

    }


}
