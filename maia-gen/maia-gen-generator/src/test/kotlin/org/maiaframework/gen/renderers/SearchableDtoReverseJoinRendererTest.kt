package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ApplicationModelDef
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.ReferencedEntity
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File


class SearchableDtoReverseJoinRendererTest {


    private class ReverseJoinSpec(autoRefresh: Boolean = true) : AbstractSpec(AppKey("Test")) {

        val region = entity("com.example", "Region") {
            field("name", FieldTypes.string)
            crud {}
        }

        val customer = entity("com.example", "Customer") {
            field("name", FieldTypes.string)
            foreignKey("region", region)
            crud {}
        }

        val order = entity("com.example", "Order") {
            field("orderNo", FieldTypes.string)
            foreignKey("customer", customer)
            crud {}
        }

        val orderDto = searchableDto(baseName = "OrderEs", entityDef = order) {
            elasticsearchEnabled(autoRefresh = autoRefresh)
            field("orderNo")
            field("customerName", "customer.name")
            field("customerRegionName", "customer.region.name")
        }

        val orderPlainDto = searchableDto(baseName = "OrderPlain", entityDef = order) {
            field("orderNo")
            field("customerName", "customer.name")
            field("customerRegionName", "customer.region.name")
        }

        val shipment = entity("com.example", "Shipment") {
            foreignKey("billingCustomer", customer)
            foreignKey("shippingCustomer", customer)
            crud {}
        }

        val shipmentDto = searchableDto(baseName = "ShipmentEs", entityDef = shipment) {
            elasticsearchEnabled()
            field("billingName", "billingCustomer.name")
            field("shippingName", "shippingCustomer.name")
        }

        val left = entity("com.example", "Left", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
            crud {}
        }

        val right = entity("com.example", "Right", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
            crud {}
        }

        val leftRightSimple = simpleManyToManyEntity(
            "com.example",
            "LeftRightSimple",
            leftEntity = ReferencedEntity("left", "Left", left, IsEditableByUser.TRUE),
            rightEntity = ReferencedEntity("right", "Right", right, IsEditableByUser.TRUE)
        )

        val leftDto = searchableDto(baseName = "LeftEs", entityDef = left) {
            elasticsearchEnabled()
            field("name")
            manyToManyField("rights", leftRightSimple)
        }

        val leftRightCrud = manyToManyEntity(
            "com.example",
            "LeftRightCrud",
            leftEntity = ReferencedEntity("left", "Left", left, IsEditableByUser.TRUE),
            rightEntity = ReferencedEntity("right", "Right", right, IsEditableByUser.TRUE)
        ) {
            field("weight", FieldTypes.int)
            crud {}
        }

        val leftCrudJoinDto = searchableDto(baseName = "LeftCrudJoinEs", entityDef = left) {
            elasticsearchEnabled()
            field("name")
            manyToManyField("rights", leftRightCrud)
        }

        val vehicle = entity("com.example", "Vehicle") {
            typeDiscriminator("VEHICLE")
            field("name", FieldTypes.string)
            foreignKey("region", region)
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
            field("regionName", "region.name")
        }

        val garage = entity("com.example", "Garage") {
            field("label", FieldTypes.string)
            foreignKey("car", car)
            crud {}
        }

        val garageDto = searchableDto(baseName = "GarageEs", entityDef = garage) {
            elasticsearchEnabled()
            field("label")
            field("carDoors", "car.doors")
        }

    }


    /** Entities without crud {}: reading esRefreshDependencies would fail validation. */
    private class NoCrudSpec : AbstractSpec(AppKey("Test")) {

        val parent = entity("com.example", "Parent") {
            field("name", FieldTypes.string)
        }

        val child = entity("com.example", "Child") {
            field("title", FieldTypes.string)
            foreignKey("parent", parent)
        }

        val noAutoRefreshDto = searchableDto(baseName = "ChildEs", entityDef = child) {
            elasticsearchEnabled(autoRefresh = false)
            field("title")
            field("parentName", "parent.name")
        }

        val plainDto = searchableDto(baseName = "ChildPlain", entityDef = child) {
            field("title")
            field("parentName", "parent.name")
        }

    }


    private val q = "\"\"\""


    private val spec = ReverseJoinSpec()


    private val applicationModelDef = applicationModelDefFor(spec)


    private fun applicationModelDefFor(abstractSpec: AbstractSpec): ApplicationModelDef {

        return object : ApplicationSpec("com.example") {
            override val modelDefs: List<ModelDef> = listOf(abstractSpec.modelDef)
        }.applicationModelDef

    }


    private fun renderDao(dto: SearchableDtoDef, dir: File, modelDef: ApplicationModelDef = applicationModelDef): String {

        SearchableDtoJdbcDaoRenderer(dto, modelDef).renderToDir(dir)
        return SearchableDtoEsTestSupport.singleKotlinFileIn(dir)

    }


    private fun renderRepo(dto: SearchableDtoDef, dir: File): String {

        SearchableDtoRepoRenderer(dto).renderToDir(dir)
        return SearchableDtoEsTestSupport.singleKotlinFileIn(dir)

    }


    private fun functionText(source: String, signaturePrefix: String): String {

        assertThat(source).contains(signaturePrefix)
        return signaturePrefix + source.substringAfter(signaturePrefix).substringBefore("\n    }\n") + "\n    }\n"

    }


    private fun reverseJoinFunctionNames(source: String): List<String> {

        return Regex("fun (findRootIdsVia\\w+)\\(").findAll(source).map { it.groupValues[1] }.toList()

    }


    private fun daoFunction(name: String, vararg sqlLines: String): String {

        return listOf(
            "    fun $name(id: DomainId): List<DomainId> {",
            "",
            "        val sqlParams = SqlParams().addValue(\"id\", id)",
            "",
            "        val sql = $q",
            *sqlLines,
            "            $q.trimIndent()",
            "",
            "        return this.jdbcOps.queryForList(sql, sqlParams) { rsa -> rsa.readDomainId(\"id\") }",
            "",
            "    }",
            ""
        ).joinToString("\n")

    }


    @Test
    fun `chain of foreign keys renders one reverse join per non-root dependency`(@TempDir tempDir: File) {

        val dao = renderDao(spec.orderDto, tempDir)

        assertThat(reverseJoinFunctionNames(dao)).containsExactly("findRootIdsViaCustomer", "findRootIdsViaRegion")

        assertThat(functionText(dao, "    fun findRootIdsViaCustomer(")).isEqualTo(
            daoFunction(
                "findRootIdsViaCustomer",
                "            select distinct test.order.id as id",
                "            from test.order",
                "            inner join test.customer h0",
                "                    on test.order.customer_id = h0.id",
                "            where h0.id = :id"
            )
        )

        assertThat(functionText(dao, "    fun findRootIdsViaRegion(")).isEqualTo(
            daoFunction(
                "findRootIdsViaRegion",
                "            select distinct test.order.id as id",
                "            from test.order",
                "            inner join test.customer h0",
                "                    on test.order.customer_id = h0.id",
                "            inner join test.region h1",
                "                    on h0.region_id = h1.id",
                "            where h1.id = :id"
            )
        )

        assertThat(dao).doesNotContain("findRootIdsViaOrder")

    }


    @Test
    fun `reverse join foreign key columns are the same columns the forward joins use`(@TempDir tempDir: File) {

        val dao = renderDao(spec.orderDto, tempDir)
        val findById = functionText(dao, "    fun findById(")

        assertThat(findById).contains("test.order.customer_id")
        assertThat(findById).contains("test.customer.region_id")

    }


    @Test
    fun `two paths to the same entity are combined with union`(@TempDir tempDir: File) {

        val dao = renderDao(spec.shipmentDto, tempDir)

        assertThat(reverseJoinFunctionNames(dao)).containsExactly("findRootIdsViaCustomer")

        assertThat(functionText(dao, "    fun findRootIdsViaCustomer(")).isEqualTo(
            daoFunction(
                "findRootIdsViaCustomer",
                "            select distinct test.shipment.id as id",
                "            from test.shipment",
                "            inner join test.customer h0",
                "                    on test.shipment.billing_customer_id = h0.id",
                "            where h0.id = :id",
                "            union",
                "            select distinct test.shipment.id as id",
                "            from test.shipment",
                "            inner join test.customer h0",
                "                    on test.shipment.shipping_customer_id = h0.id",
                "            where h0.id = :id"
            )
        )

    }


    @Test
    fun `many-to-many other side joins through the join table and a join entity without listener gets no method`(@TempDir tempDir: File) {

        val dao = renderDao(spec.leftDto, tempDir)

        assertThat(reverseJoinFunctionNames(dao)).containsExactly("findRootIdsViaRight")

        assertThat(functionText(dao, "    fun findRootIdsViaRight(")).isEqualTo(
            daoFunction(
                "findRootIdsViaRight",
                "            select distinct test.left.id as id",
                "            from test.left",
                "            inner join test.left_right_simple h0",
                "                    on test.left.id = h0.left_id",
                "            inner join test.right h1",
                "                    on h0.right_id = h1.id",
                "            where h1.id = :id"
            )
        )

    }


    @Test
    fun `many-to-many join entity with a crud listener gets no method because its listener reads the root foreign key`(@TempDir tempDir: File) {

        val dao = renderDao(spec.leftCrudJoinDto, tempDir.resolve("dao"))
        val repo = renderRepo(spec.leftCrudJoinDto, tempDir.resolve("repo"))

        assertThat(reverseJoinFunctionNames(dao)).containsExactly("findRootIdsViaRight")
        assertThat(reverseJoinFunctionNames(repo)).containsExactly("findRootIdsViaRight")
        assertThat(dao).doesNotContain("findRootIdsViaLeftRightCrud")

    }


    @Test
    fun `many-to-many join entity with a crud listener and a non DomainId primary key gets no method`(@TempDir tempDir: File) {

        val listenerSpec = EsRefreshListenerTestSpec()
        val dto = listenerSpec.leftCodedDto

        assertThat(dto.esRefreshDependencies.map { it.entityDef.entityBaseName.value }).containsExactly("Left", "LeftRightCoded")
        assertThat(dto.esRefreshDependencies[1].hasCrudListener).isTrue()

        val dao = renderDao(dto, tempDir.resolve("dao"), applicationModelDefFor(listenerSpec))

        assertThat(reverseJoinFunctionNames(dao)).isEmpty()
        assertThat(reverseJoinFunctionNames(renderRepo(dto, tempDir.resolve("repo")))).isEmpty()

    }


    @Test
    fun `subclass root applies its discriminator predicate to every reverse join`(@TempDir tempDir: File) {

        val dao = renderDao(spec.carEsDto, tempDir)

        assertThat(reverseJoinFunctionNames(dao)).containsExactly("findRootIdsViaRegion")

        assertThat(functionText(dao, "    fun findRootIdsViaRegion(")).isEqualTo(
            daoFunction(
                "findRootIdsViaRegion",
                "            select distinct test.vehicle.id as id",
                "            from test.vehicle",
                "            inner join test.region h0",
                "                    on test.vehicle.region_id = h0.id",
                "            where h0.id = :id",
                "            and test.vehicle.type_discriminator = 'CAR'"
            )
        )

    }


    @Test
    fun `hop into a subclass entity applies that entity's discriminator on the hop alias`(@TempDir tempDir: File) {

        val dao = renderDao(spec.garageDto, tempDir)

        assertThat(reverseJoinFunctionNames(dao)).containsExactly("findRootIdsViaCar")

        assertThat(functionText(dao, "    fun findRootIdsViaCar(")).isEqualTo(
            daoFunction(
                "findRootIdsViaCar",
                "            select distinct test.garage.id as id",
                "            from test.garage",
                "            inner join test.vehicle h0",
                "                    on test.garage.car_id = h0.id",
                "            where h0.id = :id",
                "            and h0.type_discriminator = 'CAR'"
            )
        )

    }


    @Test
    fun `repo passes through exactly the reverse joins the dao renders`(@TempDir tempDir: File) {

        listOf(spec.orderDto, spec.shipmentDto, spec.leftDto, spec.leftCrudJoinDto, spec.carEsDto, spec.garageDto, spec.orderPlainDto)
            .forEachIndexed { index, dto ->

                val dao = renderDao(dto, tempDir.resolve("dao$index"))
                val repo = renderRepo(dto, tempDir.resolve("repo$index"))

                assertThat(reverseJoinFunctionNames(repo)).isEqualTo(reverseJoinFunctionNames(dao))

            }

        val repo = renderRepo(spec.orderDto, tempDir.resolve("order-repo"))

        assertThat(functionText(repo, "    fun findRootIdsViaRegion(")).isEqualTo(
            listOf(
                "    fun findRootIdsViaRegion(id: DomainId): List<DomainId> {",
                "",
                "        return this.dao.findRootIdsViaRegion(id)",
                "",
                "    }",
                ""
            ).joinToString("\n")
        )

        assertThat(repo).contains("import org.maiaframework.domain.DomainId")

    }


    @Test
    fun `non ES dto renders no reverse joins`(@TempDir tempDir: File) {

        assertThat(renderDao(spec.orderPlainDto, tempDir.resolve("dao"))).doesNotContain("findRootIdsVia")
        assertThat(renderRepo(spec.orderPlainDto, tempDir.resolve("repo"))).doesNotContain("findRootIdsVia")

    }


    @Test
    fun `ES dto with only autoRefresh false versions renders exactly as without reverse joins and never validates`(@TempDir tempDir: File) {

        val noAutoRefreshSpec = ReverseJoinSpec(autoRefresh = false)
        val noAutoRefreshModel = applicationModelDefFor(noAutoRefreshSpec)

        val withoutAutoRefresh = renderDao(noAutoRefreshSpec.orderDto, tempDir.resolve("off"), noAutoRefreshModel)
        val withAutoRefresh = renderDao(spec.orderDto, tempDir.resolve("on"))

        assertThat(withoutAutoRefresh).doesNotContain("findRootIdsVia")
        assertThat(withoutAutoRefresh).contains("fun findByIds(ids: Collection<DomainId>): List<OrderEsDto> {")
        assertThat(withoutAutoRefresh).contains("fun findAllAsSequence(): Sequence<OrderEsDto> {")

        val withAutoRefreshMinusReverseJoins = withAutoRefresh
            .replace("\n\n" + functionText(withAutoRefresh, "    fun findRootIdsViaCustomer("), "")
            .replace("\n\n" + functionText(withAutoRefresh, "    fun findRootIdsViaRegion("), "")

        assertThat(withAutoRefreshMinusReverseJoins).isEqualTo(withoutAutoRefresh)

        assertThat(renderRepo(noAutoRefreshSpec.orderDto, tempDir.resolve("off-repo"))).doesNotContain("findRootIdsVia")

        // entities without crud {} would fail EsRefreshDependency validation if it were read
        val noCrudSpec = NoCrudSpec()
        val noCrudModel = applicationModelDefFor(noCrudSpec)

        val noCrudDao = renderDao(noCrudSpec.noAutoRefreshDto, tempDir.resolve("no-crud"), noCrudModel)
        assertThat(noCrudDao).doesNotContain("findRootIdsVia")
        assertThat(noCrudDao).contains("fun findByIds(")

        assertThat(renderDao(noCrudSpec.plainDto, tempDir.resolve("no-crud-plain"), noCrudModel)).doesNotContain("findRootIdsVia")
        assertThat(renderRepo(noCrudSpec.noAutoRefreshDto, tempDir.resolve("no-crud-repo"))).doesNotContain("findRootIdsVia")

    }


    @Test
    fun `rendering is deterministic`(@TempDir tempDir: File) {

        listOf(spec.orderDto, spec.shipmentDto, spec.leftCrudJoinDto, spec.garageDto).forEachIndexed { index, dto ->

            val first = renderDao(dto, tempDir.resolve("a$index"))
            val second = renderDao(dto, tempDir.resolve("b$index"))
            val otherSpecInstance = ReverseJoinSpec()
            val third = renderDao(
                listOf(otherSpecInstance.orderDto, otherSpecInstance.shipmentDto, otherSpecInstance.leftCrudJoinDto, otherSpecInstance.garageDto)[index],
                tempDir.resolve("c$index"),
                applicationModelDefFor(otherSpecInstance)
            )

            assertThat(second).isEqualTo(first)
            assertThat(third).isEqualTo(first)

        }

    }


}
