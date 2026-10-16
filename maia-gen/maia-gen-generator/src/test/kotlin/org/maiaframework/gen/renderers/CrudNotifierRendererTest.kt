package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.renderers.CrudListenerTestSupport.functionBody
import org.maiaframework.gen.renderers.CrudListenerTestSupport.singleKotlinFileIn
import org.maiaframework.gen.renderers.CrudListenerTestSupport.spec
import org.maiaframework.gen.spec.definition.EntityDef
import java.io.File

class CrudNotifierRendererTest {


    private fun render(entityDef: EntityDef, dir: File): String {

        CrudNotifierRenderer(entityDef).renderToDir(dir)
        return singleKotlinFileIn(dir)

    }


    @Test
    fun `onEntityUpdated receives before and after and fans them out`(@TempDir dir: File) {

        val source = render(spec.widget, dir)

        val body = functionBody(source, "    fun onEntityUpdated(before: WidgetEntity, after: WidgetEntity) {")

        assertThat(body).contains("this.widgetCrudListeners.forEach { (_, listener) ->")
        assertThat(body).contains("listener.onWidgetEntityUpdated(before, after)")
        assertThat(source).doesNotContain("import org.maiaframework.domain.DomainId")

    }


    @Test
    fun `onEntityBeforeDelete fans out to the before-delete hook`(@TempDir dir: File) {

        val source = render(spec.widget, dir)

        val body = functionBody(source, "    fun onEntityBeforeDelete(entity: WidgetEntity) {")

        assertThat(body).contains("this.widgetCrudListeners.forEach { (_, listener) ->")
        assertThat(body).contains("listener.onWidgetEntityBeforeDelete(entity)")
        assertThat(body).doesNotContain("Deleted(")

        val deletedBody = functionBody(source, "    fun onEntityDeleted(entity: WidgetEntity) {")
        assertThat(deletedBody).contains("listener.onWidgetEntityDeleted(entity)")

    }


    @Test
    fun `notifier of a non-deletable entity has no delete functions`(@TempDir dir: File) {

        val source = render(spec.sprocket, dir)

        assertThat(source).contains("fun onEntityUpdated(before: SprocketEntity, after: SprocketEntity) {")
        assertThat(source).doesNotContain("onEntityBeforeDelete")
        assertThat(source).doesNotContain("onEntityDeleted")

    }


    @Test
    fun `subclass notifier also fans updates out to superclass listeners`(@TempDir dir: File) {

        val source = render(spec.car, dir)

        val body = functionBody(source, "    fun onEntityUpdated(before: CarEntity, after: CarEntity) {")

        assertThat(body).contains("this.carCrudListeners.forEach { (_, listener) ->")
        assertThat(body).contains("listener.onCarEntityUpdated(before, after)")
        assertThat(body).contains("this.vehicleCrudListeners.forEach { (_, listener) ->")
        assertThat(body).contains("listener.onVehicleEntityUpdated(before, after)")

    }


}
