package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.renderers.CrudListenerTestSupport.functionBody
import org.maiaframework.gen.renderers.CrudListenerTestSupport.singleKotlinFileIn
import org.maiaframework.gen.renderers.CrudListenerTestSupport.spec
import java.io.File

class TypeaheadCrudListenerRendererTest {


    @Test
    fun `typeahead listener refreshes the index from the after entity on update`(@TempDir dir: File) {

        TypeaheadCrudListenerRenderer(spec.widgetTypeahead).renderToDir(dir)
        val source = singleKotlinFileIn(dir)

        assertThat(source).contains(": WidgetCrudListener")

        val updatedBody = functionBody(source, "    override fun onWidgetEntityUpdated(before: WidgetEntity, after: WidgetEntity) {")
        assertThat(updatedBody).contains("this.indexService.refreshById(after.id)")

        assertThat(functionBody(source, "    override fun onWidgetEntityCreated(entity: WidgetEntity) {"))
            .contains("this.indexService.refreshById(entity.id)")
        assertThat(functionBody(source, "    override fun onWidgetEntityDeleted(entity: WidgetEntity) {"))
            .contains("this.indexService.deleteById(entity.id)")

        assertThat(source).doesNotContain("BeforeDelete")
        assertThat(source).doesNotContain("import org.maiaframework.domain.DomainId")

    }


}
