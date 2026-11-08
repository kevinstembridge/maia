package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File


class HandBuiltEsDocCrudListenerRendererTest {


    private class PartySpec(autoRefresh: Boolean = true) : AbstractSpec(AppKey("Test")) {

        val party = entity("com.example.party", "Party", deletable = Deletable.TRUE) {
            isAbstract = true
            field("displayName", FieldTypes.string) {
                fieldDisplayName("displayName")
                editableByUser()
            }
            crud {
                create { api {} }
                update { api {} }
                delete { api {} }
            }
        }

        val person = entity("com.example.party", "Person", deletable = Deletable.TRUE) {
            superclass(party)
            typeDiscriminator("PERSON")
            field("firstName", FieldTypes.string) {
                fieldDisplayName("firstName")
                editableByUser()
            }
            crud {
                create { api {} }
                update { api {} }
                delete { api {} }
            }
        }

        val partyEmail = entity("com.example.party", "PartyEmail", deletable = Deletable.TRUE) {
            foreignKey("party", party, isEditableByUser = IsEditableByUser.TRUE, fieldDisplayName = "Party")
            field("email", FieldTypes.string) {
                fieldDisplayName("email")
                editableByUser()
            }
            crud {
                create { api {} }
                update { api {} }
                delete { api {} }
            }
        }

        val partyEsDoc = esDoc("com.example.party", "Party", indexDescription = "Party summary") {
            if (autoRefresh) {
                autoRefresh(true)
                sourceEntity(party, "displayName")
                watchesSubclass(person, "firstName")
                watchesChild(partyEmail, "party", "email")
            } else {
                autoRefresh(false)
            }
            field("id", FieldTypes.domainId) { isIdField() }
        }

    }


    private fun render(spec: PartySpec, dir: File): Map<String, String> {

        EsRefreshListeners.renderersFor(spec.partyEsDoc).forEach { it.renderToDir(dir) }

        return SearchableDtoEsTestSupport.kotlinFilesIn(dir).associate { it.nameWithoutExtension to it.readText() }.toSortedMap()

    }


    @Test
    fun `renders one listener per declared source entity`(@TempDir dir: File) {

        val rendered = render(PartySpec(), dir)

        assertThat(rendered.keys).containsExactly(
            "PartyV1EsRefreshPartyCrudListener",
            "PartyV1EsRefreshPartyEmailCrudListener",
            "PartyV1EsRefreshPersonCrudListener"
        )

    }


    @Test
    fun `listener is a service implementing the entity crud listener with the index service`(@TempDir dir: File) {

        val source = render(PartySpec(), dir).getValue("PartyV1EsRefreshPartyCrudListener")

        assertThat(source).contains("package com.example.party")
        assertThat(source).contains("@Service")
        assertThat(source).contains(") : PartyCrudListener {")
        assertThat(source).contains("private val collector: EsRefreshCollector")
        assertThat(source).contains("private val indexService: PartyV1IndexService")
        assertThat(source).doesNotContain("repo")

    }


    @Test
    fun `source entity watching named fields refreshes its own id only when they change`(@TempDir dir: File) {

        val source = render(PartySpec(), dir).getValue("PartyV1EsRefreshPartyCrudListener")

        assertThat(source).contains("this.collector.enlistRefresh(this.indexService, listOf(entity.id))")
        assertThat(source).contains("if (before.displayName != after.displayName) {")

    }


    @Test
    fun `subclass refreshes the shared id when its watched fields change`(@TempDir dir: File) {

        val source = render(PartySpec(), dir).getValue("PartyV1EsRefreshPersonCrudListener")

        assertThat(source).contains(") : PersonCrudListener {")
        assertThat(source).contains("if (before.firstName != after.firstName) {")
        assertThat(source).contains("this.collector.enlistRefresh(this.indexService, listOf(after.id))")

    }


    @Test
    fun `child reads the root id from its foreign key before and after an update`(@TempDir dir: File) {

        val source = render(PartySpec(), dir).getValue("PartyV1EsRefreshPartyEmailCrudListener")

        assertThat(source).contains("override fun onPartyEmailEntityCreated(entity: PartyEmailEntity) {")
        assertThat(source).contains("this.collector.enlistRefresh(this.indexService, listOf(entity.party))")
        assertThat(source).contains("if (before.email != after.email) {")
        assertThat(source).contains("this.collector.enlistRefresh(this.indexService, listOf(before.party, after.party).distinct())")
        assertThat(source).contains("override fun onPartyEmailEntityDeleted(entity: PartyEmailEntity) {")

    }


    @Test
    fun `an EsDoc with autoRefresh false renders no listeners`(@TempDir dir: File) {

        assertThat(render(PartySpec(autoRefresh = false), dir)).isEmpty()

    }


    private class CreateWithoutApiSpec : AbstractSpec(AppKey("Test")) {

        val note = entity("com.example.party", "Note") {
            field("text", FieldTypes.string)
            crud {
                create { }
            }
        }

    }


    @Test
    fun `a create operation without an API still gives the CrudListener and CrudNotifier a create callback`(@TempDir dir: File) {

        val note = CreateWithoutApiSpec().note

        CrudListenerRenderer(note).renderToDir(dir.resolve("listener"))
        CrudNotifierRenderer(note).renderToDir(dir.resolve("notifier"))

        val listener = SearchableDtoEsTestSupport.singleKotlinFileIn(dir.resolve("listener"))
        val notifier = SearchableDtoEsTestSupport.singleKotlinFileIn(dir.resolve("notifier"))

        assertThat(listener).contains("fun onNoteEntityCreated(entity: NoteEntity)")
        assertThat(notifier).contains("listener.onNoteEntityCreated(entity)")

    }


}
