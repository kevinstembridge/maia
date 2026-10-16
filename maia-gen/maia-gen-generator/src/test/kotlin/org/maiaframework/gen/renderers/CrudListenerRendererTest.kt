package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.renderers.CrudListenerTestSupport.singleKotlinFileIn
import org.maiaframework.gen.renderers.CrudListenerTestSupport.spec
import org.maiaframework.gen.spec.definition.EntityDef
import java.io.File

class CrudListenerRendererTest {


    private fun render(entityDef: EntityDef, dir: File): String {

        CrudListenerRenderer(entityDef).renderToDir(dir)
        return singleKotlinFileIn(dir)

    }


    @Test
    fun `listener update method receives the before and after entities`(@TempDir dir: File) {

        val source = render(spec.widget, dir)

        assertThat(source).contains("    fun onWidgetEntityCreated(entity: WidgetEntity)\n")
        assertThat(source).contains("    fun onWidgetEntityUpdated(before: WidgetEntity, after: WidgetEntity)\n")
        assertThat(source).doesNotContain("onWidgetEntityUpdated(id")
        assertThat(source).doesNotContain("import org.maiaframework.domain.DomainId")

    }


    @Test
    fun `listener has a default no-op before-delete hook and a deleted method`(@TempDir dir: File) {

        val source = render(spec.widget, dir)

        assertThat(source).contains("    fun onWidgetEntityBeforeDelete(entity: WidgetEntity) {}\n")
        assertThat(source).contains("    fun onWidgetEntityDeleted(entity: WidgetEntity)\n")

    }


    @Test
    fun `listener of a non-deletable entity has no delete hooks`(@TempDir dir: File) {

        val source = render(spec.sprocket, dir)

        assertThat(source).contains("    fun onSprocketEntityUpdated(before: SprocketEntity, after: SprocketEntity)\n")
        assertThat(source).doesNotContain("BeforeDelete")
        assertThat(source).doesNotContain("Deleted")

    }


    @Test
    fun `listener of a composite primary key entity uses entities rather than key fields`(@TempDir dir: File) {

        val source = render(spec.pairing, dir)

        assertThat(source).contains("    fun onPairingEntityUpdated(before: PairingEntity, after: PairingEntity)\n")
        assertThat(source).contains("    fun onPairingEntityBeforeDelete(entity: PairingEntity) {}\n")

    }


}
