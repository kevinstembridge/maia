package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ApplicationModelDef
import org.maiaframework.gen.spec.definition.JoinType
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.ReferencedEntity
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Nullability
import java.io.File

class SearchableDtoJdbcDaoRendererTest {


    private class TestSpec : AbstractSpec(AppKey("Test")) {

        val parent = entity("com.example", "Parent") {
            field("name", FieldTypes.string)
            crud {}
        }

        val child = entity("com.example", "Child") {
            field("title", FieldTypes.string)
            foreignKey("parent", parent)
            crud {}
        }

        val esDto = searchableDto(baseName = "ChildEs", entityDef = child) {
            elasticsearchEnabled()
            field("title")
            field("parentName", "parent.name")
        }

        val plainDto = searchableDto(baseName = "ChildPlain", entityDef = child) {
            field("title")
            field("parentName", "parent.name")
        }

        val singleTableDto = searchableDto(baseName = "ParentOnly", entityDef = parent) {
            generateFindByIdStack()
            field("name")
        }

        val singleTableEsDto = searchableDto(baseName = "ParentEs", entityDef = parent) {
            elasticsearchEnabled()
            field("name")
        }

        val vehicle = entity("com.example", "Vehicle") {
            typeDiscriminator("VEHICLE")
            field("name", FieldTypes.string)
            crud {}
        }

        val car = entity("com.example", "Car") {
            superclass(vehicle)
            typeDiscriminator("CAR")
            field("doors", FieldTypes.int)
            crud {}
        }

        val carEsDto = searchableDto(baseName = "CarEs", entityDef = car) {
            elasticsearchEnabled()
            field("name")
            field("doors")
        }

        val owner = entity("com.example", "Owner", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
            crud {}
        }

        val asset = entity("com.example", "Asset", nameFieldForPkAndNameDto = "label") {
            field("label", FieldTypes.string)
            crud {}
        }

        val ownerAsset = manyToManyEntity(
            "com.example",
            "OwnerAsset",
            leftEntity = ReferencedEntity("owner", "Owner", owner, IsEditableByUser.TRUE),
            rightEntity = ReferencedEntity("asset", "Asset", asset, IsEditableByUser.TRUE)
        ) {
            withEffectiveTimestamps(hasSingleEffectiveRecord = true)
            crud {}
        }

        val assetWithOwnerOuterDto = searchableDto(baseName = "AssetWithOwnerOuter", entityDef = asset) {
            manyToManyJoin(ownerAsset, JoinType.LEFT_OUTER)
            field("label")
            field("ownerName", ownerAsset.entityDef.fieldPathOf("owner.name"))
        }

        val assetWithOwnerInnerDto = searchableDto(baseName = "AssetWithOwnerInner", entityDef = asset) {
            manyToManyJoin(ownerAsset)
            field("label")
            field("ownerName", ownerAsset.entityDef.fieldPathOf("owner.name"))
        }

    }


    private val spec = TestSpec()


    private val applicationModelDef: ApplicationModelDef = object : ApplicationSpec("com.example") {
        override val modelDefs: List<ModelDef> = listOf(spec.modelDef)
    }.applicationModelDef


    private fun renderDao(dto: SearchableDtoDef, dir: File): String {

        SearchableDtoJdbcDaoRenderer(dto, applicationModelDef).renderToDir(dir)
        return singleKotlinFileIn(dir)

    }


    private fun renderRepo(dto: SearchableDtoDef, dir: File): String {

        SearchableDtoRepoRenderer(dto).renderToDir(dir)
        return singleKotlinFileIn(dir)

    }


    private fun singleKotlinFileIn(dir: File): String {

        val files = dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        assertThat(files).withFailMessage("Expected exactly one rendered .kt file but found: $files").hasSize(1)
        return files.single().readText()

    }


    private fun functionBody(source: String, signaturePrefix: String): String {

        assertThat(source).contains(signaturePrefix)
        return signaturePrefix + source.substringAfter(signaturePrefix).substringBefore("\n    }\n")

    }


    private fun selectList(functionBody: String): String {

        return functionBody.substringAfter("select").substringBefore("from ")

    }


    private fun selectToWhere(functionBody: String): String {

        return functionBody.substringAfter("select").substringBefore("where")

    }


    @Test
    fun `a field read through a many-to-many join entity joins the link table from the root`(@TempDir tempDir: File) {

        val dao = renderDao(spec.assetWithOwnerOuterDto, tempDir)
        val body = functionBody(dao, "fun search(searchModel: AgGridSearchModel): SearchResultPage<AssetWithOwnerOuterDto> {")

        val linkJoin = body.indexOf("left outer join test.owner_asset\n")
        val ownerJoin = body.indexOf("left outer join test.owner\n")
        assertThat(linkJoin).isPositive()
        assertThat(ownerJoin).isPositive()
        assertThat(body).contains("on test.asset.id = test.owner_asset.asset_id")
        assertThat(body).contains("and test.owner_asset.effective_range @> current_timestamp")
        assertThat(body).contains("on test.owner_asset.owner_id = test.owner.id")
        assertThat(ownerJoin).isGreaterThan(linkJoin)

    }


    @Test
    fun `an inner many-to-many join renders inner joins and a non-nullable field`(@TempDir tempDir: File) {

        val dao = renderDao(spec.assetWithOwnerInnerDto, tempDir)

        assertThat(dao).contains("inner join test.owner_asset")
        assertThat(dao).doesNotContain("outer join")
        assertThat(spec.assetWithOwnerInnerDto.allFieldsSorted.first { it.classFieldName.value == "ownerName" }.nullability).isEqualTo(Nullability.NOT_NULLABLE)
        assertThat(spec.assetWithOwnerOuterDto.allFieldsSorted.first { it.classFieldName.value == "ownerName" }.nullability).isEqualTo(Nullability.NULLABLE)

    }


    @Test
    fun `ES enabled dto renders findByIds with the empty input guard`(@TempDir tempDir: File) {

        val dao = renderDao(spec.esDto, tempDir)

        assertThat(dao).contains("fun findByIds(ids: Collection<DomainId>): List<")
        assertThat(dao).contains("if (ids.isEmpty()) return emptyList()")

    }


    @Test
    fun `findByIds qualifies the root id and binds the ids as OTHER values`(@TempDir tempDir: File) {

        val dao = renderDao(spec.esDto, tempDir)

        assertThat(dao).contains("where test.child.id in (:ids)")
        assertThat(dao).doesNotContain("where id in")
        assertThat(dao).contains("ids.map { it.value }, Types.OTHER)")
        assertThat(dao).contains("import java.sql.Types")
        assertThat(dao).contains("import org.maiaframework.domain.DomainId")
        assertThat(dao).contains("this.jdbcOps.queryForList(sql, sqlParams, this.dtoRowMapper)")

    }


    @Test
    fun `ES enabled dto renders findAllAsSequence using queryForSequence`(@TempDir tempDir: File) {

        val dao = renderDao(spec.esDto, tempDir)
        val body = functionBody(dao, "fun findAllAsSequence(): Sequence<ChildEsDto> {")

        assertThat(body).contains("return this.jdbcOps.queryForSequence(sql, SqlParams(), this.dtoRowMapper)")
        assertThat(body).contains("from test.child")
        assertThat(body).contains("inner join test.parent")

    }


    @Test
    fun `findByIds uses the same select list and joins as findById`(@TempDir tempDir: File) {

        val dao = renderDao(spec.esDto, tempDir)

        val findById = functionBody(dao, "fun findById(")
        val findByIds = functionBody(dao, "fun findByIds(")

        assertThat(selectToWhere(findById)).contains("inner join")
        assertThat(selectToWhere(findByIds)).isEqualTo(selectToWhere(findById))

    }


    @Test
    fun `findAllAsSequence uses the same select list and joins as findById`(@TempDir tempDir: File) {

        val dao = renderDao(spec.esDto, tempDir)

        val findById = functionBody(dao, "fun findById(")
        val findAll = functionBody(dao, "fun findAllAsSequence(")

        assertThat(selectList(findAll)).isEqualTo(selectList(findById))
        assertThat(findAll.substringAfter("from ").substringBefore("\"\"\".trimIndent()"))
            .isEqualTo(findById.substringAfter("from ").substringBefore("where"))

    }


    @Test
    fun `findById qualifies the root id for a joined dto`(@TempDir tempDir: File) {

        val dao = renderDao(spec.esDto, tempDir)

        assertThat(dao).contains("where test.child.id = :id")
        assertThat(dao).doesNotContain("where id = :id")

    }


    @Test
    fun `findById qualifies the root id for a single table dto`(@TempDir tempDir: File) {

        val dao = renderDao(spec.singleTableDto, tempDir)

        assertThat(dao).contains("where test.parent.id = :id")
        assertThat(dao).doesNotContain("where id = :id")

    }


    @Test
    fun `findById full text is pinned for a single table non ES dto`(@TempDir tempDir: File) {

        val dao = renderDao(spec.singleTableDto, tempDir)

        val q = "\"\"\""
        val expected = listOf(
            "    fun findById(id: DomainId): ParentOnlyDto? {",
            "",
            "        val sqlParams = SqlParams().addValue(\"id\", id)",
            "",
            "        val sql = $q",
            "            select ",
            "                test.parent.id as id,",
            "                test.parent.name as name",
            "            from test.parent",
            "            where test.parent.id = :id",
            "            $q.trimIndent()",
            "",
            "        return this.jdbcOps.queryForObjectOrNull(sql, sqlParams, this.dtoRowMapper)",
            "",
            "    }",
            ""
        ).joinToString("\n")

        assertThat(dao).contains(expected)
        assertThat(dao).doesNotContain("findByIds")
        assertThat(dao).doesNotContain("findAllAsSequence")

    }


    @Test
    fun `non ES dto renders neither findByIds nor findAllAsSequence`(@TempDir tempDir: File) {

        val dao = renderDao(spec.plainDto, tempDir)

        assertThat(dao).doesNotContain("findByIds")
        assertThat(dao).doesNotContain("findAllAsSequence")

    }


    @Test
    fun `findAllAsSequence has no where clause for a plain entity`(@TempDir tempDir: File) {

        val dao = renderDao(spec.singleTableEsDto, tempDir)
        val body = functionBody(dao, "fun findAllAsSequence(): Sequence<ParentEsDto> {")

        assertThat(body).doesNotContain("where")
        assertThat(body).doesNotContain("type_discriminator")

        val findByIds = functionBody(dao, "fun findByIds(")
        assertThat(findByIds).contains("where test.parent.id in (:ids)")
        assertThat(findByIds).doesNotContain("type_discriminator")

    }


    @Test
    fun `findAllAsSequence and findByIds apply the type discriminator for a subclass dto`(@TempDir tempDir: File) {

        val dao = renderDao(spec.carEsDto, tempDir)

        val findAll = functionBody(dao, "fun findAllAsSequence(")
        val findByIds = functionBody(dao, "fun findByIds(")

        assertThat(findAll).contains("where test.vehicle.type_discriminator = 'CAR'")
        assertThat(findByIds).contains("where test.vehicle.id in (:ids)")
        assertThat(findByIds).contains("and test.vehicle.type_discriminator = 'CAR'")
        // the existing search/count expression is still the unqualified form
        assertThat(dao).contains("private val typeDiscriminatorExpression = \"type_discriminator = 'CAR'\"")

    }


    @Test
    fun `repo passes through findByIds and findAllAsSequence for ES enabled dto`(@TempDir tempDir: File) {

        val repo = renderRepo(spec.esDto, tempDir)

        assertThat(repo).contains("fun findByIds(ids: Collection<DomainId>): List<ChildEsDto> {")
        assertThat(repo).contains("return this.dao.findByIds(ids)")
        assertThat(repo).contains("fun findAllAsSequence(): Sequence<ChildEsDto> {")
        assertThat(repo).contains("return this.dao.findAllAsSequence()")
        assertThat(repo).contains("import org.maiaframework.domain.DomainId")

    }


    @Test
    fun `repo renders neither passthrough for a non ES dto`(@TempDir tempDir: File) {

        val repo = renderRepo(spec.plainDto, tempDir)

        assertThat(repo).doesNotContain("findByIds")
        assertThat(repo).doesNotContain("findAllAsSequence")

    }


}
