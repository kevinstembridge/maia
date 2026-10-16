package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.renderers.CrudListenerTestSupport.applicationModelDef
import org.maiaframework.gen.renderers.CrudListenerTestSupport.assertInOrder
import org.maiaframework.gen.renderers.CrudListenerTestSupport.functionBody
import org.maiaframework.gen.renderers.CrudListenerTestSupport.singleKotlinFileIn
import org.maiaframework.gen.renderers.CrudListenerTestSupport.spec
import org.maiaframework.gen.spec.definition.EntityDef
import java.io.File

class CrudServiceListenerRendererTest {


    private fun render(entityDef: EntityDef, dir: File): String {

        CrudServiceRenderer(entityDef, applicationModelDef).renderToDir(dir)
        return singleKotlinFileIn(dir)

    }


    @Test
    fun `setFields loads the before and after entities and notifies only when a row was updated`(@TempDir dir: File) {

        val source = render(spec.widget, dir)

        val body = functionBody(source, "    fun setFields(updater: WidgetEntityUpdater): Int {")

        assertInOrder(
            body,
            "val before = this.entityRepo.findByPrimaryKeyOrNull(updater.id)",
            "val count = this.entityRepo.setFields(updater)",
            "if (count > 0 && before != null) {",
            "val after = this.entityRepo.findByPrimaryKey(updater.id)",
            "this.widgetCrudNotifier.onEntityUpdated(before, after)",
            "}",
            "return count"
        )

    }


    @Test
    fun `setFields of a composite primary key entity looks up by the primary key class`(@TempDir dir: File) {

        val source = render(spec.pairing, dir)

        val body = functionBody(source, "    fun setFields(updater: PairingEntityUpdater): Int {")

        assertInOrder(
            body,
            "val before = this.entityRepo.findByPrimaryKeyOrNull(updater.primaryKey)",
            "val count = this.entityRepo.setFields(updater)",
            "if (count > 0 && before != null) {",
            "val after = this.entityRepo.findByPrimaryKey(updater.primaryKey)",
            "this.pairingCrudNotifier.onEntityUpdated(before, after)",
            "return count"
        )

    }


    @Test
    fun `delete calls before-delete after the guards and before the row is deleted`(@TempDir dir: File) {

        val source = render(spec.widget, dir)

        val body = functionBody(source, "    fun delete(id: DomainId) {")

        assertInOrder(
            body,
            "if (this.widgetPartRepo.existsByWidget(id)) {",
            "throw this.maiaProblems.foreignKeyRecordsExist(\"WidgetPart\")",
            "val entityToDelete = this.entityRepo.findByPrimaryKeyOrNull(id)",
            "?: return",
            "this.widgetCrudNotifier.onEntityBeforeDelete(entityToDelete)",
            "this.entityRepo.deleteByPrimaryKey(id)",
            "this.widgetCrudNotifier.onEntityDeleted(entityToDelete)"
        )

    }


    @Test
    fun `entity without a crud listener does no extra loads and no notification`(@TempDir dir: File) {

        val source = render(spec.plain, dir)

        val setFieldsBody = functionBody(source, "    fun setFields(updater: PlainEntityUpdater): Int {")
        assertThat(setFieldsBody).doesNotContain("before")
        assertThat(setFieldsBody).doesNotContain("findByPrimaryKey")
        assertThat(setFieldsBody).contains("val count = this.entityRepo.setFields(updater)")

        val deleteBody = functionBody(source, "    fun delete(id: DomainId) {")
        assertThat(deleteBody).doesNotContain("entityToDelete")
        assertThat(deleteBody).doesNotContain("findByPrimaryKey")

        assertThat(source).doesNotContain("Notifier")

    }


}
