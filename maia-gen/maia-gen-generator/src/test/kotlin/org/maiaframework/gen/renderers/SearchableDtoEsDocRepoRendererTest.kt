package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.renderers.SearchableDtoEsTestSupport.singleKotlinFileIn
import org.maiaframework.gen.renderers.SearchableDtoEsTestSupport.spec
import java.io.File


class SearchableDtoEsDocRepoRendererTest {


    private fun render(version: Int, dir: File): String {

        val esDocDef = spec.esDto.esDocDefs.single { it.esDocVersion == version }
        SearchableDtoEsDocRepoRenderer(spec.esDto, esDocDef).renderToDir(dir)
        return singleKotlinFileIn(dir)

    }


    @Test
    fun `renders a spring component named after the esdoc with the dto repo as private ctor arg`(@TempDir dir: File) {

        val source = render(1, dir)

        assertThat(source).contains("@Component")
        assertThat(source).contains("class ChildEsV1EsDocRepo(")
        assertThat(source).contains("private val dtoRepo: ChildEsDtoRepo")

    }


    @Test
    fun `renders findByIds and findAllAsSequence delegating to the dto repo`(@TempDir dir: File) {

        val source = render(1, dir)

        assertThat(source).contains("fun findByIds(ids: Collection<DomainId>): List<ChildEsV1EsDoc> {")
        assertThat(source).contains("return this.dtoRepo.findByIds(ids).map { buildEsDoc(it) }")
        assertThat(source).contains("fun findAllAsSequence(): Sequence<ChildEsV1EsDoc> {")
        assertThat(source).contains("return this.dtoRepo.findAllAsSequence().map { buildEsDoc(it) }")
        assertThat(source).contains("import org.maiaframework.domain.DomainId")
        assertThat(source).doesNotContain(".toList()")

    }


    @Test
    fun `buildEsDoc uses named args for every esdoc field including the fk path field`(@TempDir dir: File) {

        val source = render(1, dir)
        val esDocDef = spec.esDto.esDocDefs.single { it.esDocVersion == 1 }

        assertThat(source).contains("private fun buildEsDoc(dto: ChildEsDto): ChildEsV1EsDoc {")
        assertThat(source).contains("return ChildEsV1EsDoc(")

        val names = esDocDef.fields.map { it.classFieldDef.classFieldName.value }
        assertThat(names).contains("parentName", "title", "id")

        names.forEach { name ->
            assertThat(source).contains("            $name = dto.$name,")
        }

    }


    @Test
    fun `each version renders its own repo class`(@TempDir dir1: File, @TempDir dir2: File) {

        assertThat(render(1, dir1)).contains("class ChildEsV1EsDocRepo(")
        assertThat(render(2, dir2)).contains("class ChildEsV2EsDocRepo(").contains("List<ChildEsV2EsDoc>")

    }


}
