package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.renderers.SearchableDtoEsTestSupport.singleKotlinFileIn
import org.maiaframework.gen.renderers.SearchableDtoEsTestSupport.spec
import java.io.File


class SearchableDtoEsIndexServiceRendererTest {


    private fun render(version: Int, dir: File): String {

        val esDocDef = spec.esDto.esDocDefs.single { it.esDocVersion == version }
        SearchableDtoEsIndexServiceRenderer(esDocDef).renderToDir(dir)
        return singleKotlinFileIn(dir)

    }


    @Test
    fun `implements EsRefreshTarget and is a component`(@TempDir dir: File) {

        val source = render(1, dir)

        assertThat(source).contains("import org.maiaframework.elasticsearch.refresh.EsRefreshTarget")
        assertThat(source).contains("@Component")
        assertThat(source).containsPattern("\\) : EsRefreshTarget \\{")
        assertThat(source).contains("class ChildEsV1IndexService(")
        assertThat(source).contains("private val esDocRepo: ChildEsV1EsDocRepo")

    }


    @Test
    fun `refreshByIds upserts found docs and deletes ids that were not found`(@TempDir dir: File) {

        val source = render(1, dir)

        assertThat(source).contains("override fun refreshByIds(ids: Collection<DomainId>) {")
        assertThat(source).contains("val docs = this.esDocRepo.findByIds(ids)")
        assertThat(source).contains("val found = docs.map { it.id.value }.toSet()")
        assertThat(source).contains("if (docs.isNotEmpty()) {")
        assertThat(source).contains("this.esIndexOps.bulkUpsert(docs.map { buildEsDocHolder(it) })")
        assertThat(source).contains("val missing = ids.map { it.value }.filter { it !in found }")
        assertThat(source).contains("if (missing.isNotEmpty()) {")
        assertThat(source).contains("this.esIndexOps.deleteByIds(missing, this.indexResolvedName)")
        assertThat(source).contains("never write through JDBC")

    }


    @Test
    fun `creates a missing index from its type mapping before the first upsert`(@TempDir dir: File) {

        val source = render(1, dir)

        assertThat(source).contains("private val esIndexControl: ChildEsEsIndexControl_v0001")
        assertThat(source).contains("import co.elastic.clients.elasticsearch._types.ElasticsearchException")
        assertThat(source).contains("private fun ensureIndexExists() {")
        assertThat(source).contains("this.elasticClient.indices().exists { r -> r.index(this.indexResolvedName.value) }.value()")
        assertThat(source).contains("this.esIndexControl.createIndex()")
        assertThat(source).contains("\"resource_already_exists_exception\"")

        val refreshByIds = source.substringAfter("override fun refreshByIds").substringBefore("private fun ensureIndexExists")
        assertThat(refreshByIds.indexOf("ensureIndexExists()")).isLessThan(refreshByIds.indexOf("bulkUpsert"))

        val refreshIndex = source.substringAfter("fun refreshIndex(jm: JobMetrics)").substringBefore("private fun upsertAllCurrentRecords")
        assertThat(refreshIndex.indexOf("ensureIndexExists()")).isLessThan(refreshIndex.indexOf("upsertAllCurrentRecords"))

    }


    @Test
    fun `renders deleteById override`(@TempDir dir: File) {

        val source = render(1, dir)

        assertThat(source).contains("override fun deleteById(id: DomainId) {")
        assertThat(source).contains("this.esIndexOps.deleteById(id.value, this.indexResolvedName)")

    }


    @Test
    fun `refreshIndex streams the sequence in chunks inside a read only transaction and never lists it`(@TempDir dir: File) {

        val source = render(1, dir)

        assertThat(source).contains("import org.springframework.transaction.annotation.Transactional")
        assertThat(source).contains("    @Transactional(readOnly = true)\n    fun refreshIndex(jm: JobMetrics) {")
        assertThat(source).contains("this.esDocRepo.findAllAsSequence().chunked(chunkSize)")
        assertThat(source).contains("childEsV1IndexService.bulkUpsert.chunkSize")
        assertThat(source).contains("removeDeletedRecordsFromIndex(currentIds, jm)")
        assertThat(source).contains("this.esIndexOps.removeDeletedRecordsFromIndex(currentIds, this.indexResolvedName, chunkSize, jm)")
        assertThat(source).contains("EsDocHolder(esDoc.id.value, esDoc, this.indexResolvedName)")
        assertThat(source).doesNotContain(".toList()")
        assertThat(source).doesNotContain("suspend fun")

    }


    @Test
    fun `two versions render two distinct index services`(@TempDir dir1: File, @TempDir dir2: File) {

        val v1 = render(1, dir1)
        val v2 = render(2, dir2)

        assertThat(v1).contains("class ChildEsV1IndexService(").doesNotContain("ChildEsV2")
        assertThat(v2).contains("class ChildEsV2IndexService(").doesNotContain("ChildEsV1")
        assertThat(v2).contains("ChildEsV2EsDocRepo")

    }


}
