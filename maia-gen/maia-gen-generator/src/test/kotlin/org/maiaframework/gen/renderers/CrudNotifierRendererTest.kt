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
    fun `created, updated and deleted are delivered after commit, each listener isolated from failures`(@TempDir dir: File) {

        val source = render(spec.widget, dir)

        listOf(
            "    fun onEntityCreated(entity: WidgetEntity) {" to "safely { listener.onWidgetEntityCreated(entity) }",
            "    fun onEntityUpdated(before: WidgetEntity, after: WidgetEntity) {" to "safely { listener.onWidgetEntityUpdated(before, after) }",
            "    fun onEntityDeleted(entity: WidgetEntity) {" to "safely { listener.onWidgetEntityDeleted(entity) }"
        ).forEach { (signature, call) ->
            val body = functionBody(source, signature)
            assertThat(body).contains("afterCommit {")
            assertThat(body).contains(call)
        }

    }


    @Test
    fun `before-update and before-delete stay inside the transaction and may veto by throwing`(@TempDir dir: File) {

        val source = render(spec.widget, dir)

        val beforeUpdate = functionBody(source, "    fun onEntityBeforeUpdate(entity: WidgetEntity) {")
        assertThat(beforeUpdate).contains("listener.onWidgetEntityBeforeUpdate(entity)")

        val beforeDelete = functionBody(source, "    fun onEntityBeforeDelete(entity: WidgetEntity) {")

        listOf(beforeUpdate, beforeDelete).forEach {
            assertThat(it).doesNotContain("afterCommit")
            assertThat(it).doesNotContain("safely")
        }

    }


    @Test
    fun `delivery runs in a post-commit scope`(@TempDir dir: File) {

        val source = render(spec.widget, dir)

        assertThat(source).contains("import org.maiaframework.domain.tx.PostCommitScope")
        assertThat(source).contains("PostCommitScope.run(delivery)")
        assertThat(source).contains("if (PostCommitScope.isActive || !TransactionSynchronizationManager.isSynchronizationActive()) {")

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
