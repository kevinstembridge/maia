package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File


class SearchableDtoEsDocCrudListenerRendererTest {


    private val spec = EsRefreshListenerTestSpec()


    /** Renders every auto-refresh listener of [dto], keyed by class name. */
    private fun renderAll(dto: SearchableDtoDef, dir: File): Map<String, String> {

        EsRefreshListeners.renderersFor(dto).forEach { it.renderToDir(dir) }

        return SearchableDtoEsTestSupport.kotlinFilesIn(dir)
            .associate { it.nameWithoutExtension to it.readText() }
            .toSortedMap()

    }


    private fun functionText(source: String, signaturePrefix: String): String {

        assertThat(source).contains(signaturePrefix)
        return signaturePrefix + source.substringAfter(signaturePrefix).substringBefore("\n    }\n") + "\n    }\n"

    }


    private fun function(signature: String, vararg bodyLines: String): String {

        return listOf("    $signature {", "", *bodyLines, "", "    }", "").joinToString("\n")

    }


    private fun overrideSignatures(source: String): Set<String> {

        return Regex("    override fun (on\\w+\\([^)]*\\))").findAll(source).map { it.groupValues[1] }.toSet()

    }


    private fun interfaceSignatures(entityDef: EntityDef, dir: File): Set<String> {

        CrudListenerRenderer(entityDef).renderToDir(dir)
        val source = SearchableDtoEsTestSupport.singleKotlinFileIn(dir)

        return Regex("    fun (on\\w+\\([^)]*\\))").findAll(source).map { it.groupValues[1] }.toSet()

    }


    /**
     * Every ES-enabled searchable DTO in the fixtures with the listener class names it must render. The
     * interface-consistency test checks every listener listed here, so a new fixture DTO belongs here too.
     */
    private val expectedListeners: Map<SearchableDtoDef, List<String>> = linkedMapOf(
        spec.orderDto to listOf(
            "OrderEsV1EsRefreshCustomerCrudListener",
            "OrderEsV1EsRefreshOrderCrudListener",
            "OrderEsV1EsRefreshRegionCrudListener",
            "OrderEsV2EsRefreshCustomerCrudListener",
            "OrderEsV2EsRefreshOrderCrudListener",
            "OrderEsV2EsRefreshRegionCrudListener"
        ),
        spec.orderMixedDto to listOf("OrderMixedEsV1EsRefreshOrderCrudListener"),
        spec.orderIdOnlyDto to listOf("OrderIdOnlyEsV1EsRefreshOrderCrudListener"),
        spec.leftDto to listOf(
            "LeftEsV1EsRefreshLeftCrudListener",
            "LeftEsV1EsRefreshLeftRightCrudCrudListener",
            "LeftEsV1EsRefreshRightCrudListener"
        ),
        spec.leftSimpleDto to listOf(
            "LeftSimpleEsV1EsRefreshLeftCrudListener",
            "LeftSimpleEsV1EsRefreshRightCrudListener"
        ),
        // the join entity reads the root id from its foreign key, so it gets a listener despite having no reverse join
        spec.leftCodedDto to listOf(
            "LeftCodedEsV1EsRefreshLeftCrudListener",
            "LeftCodedEsV1EsRefreshLeftRightCodedCrudListener"
        ),
        spec.rightDto to listOf(
            "RightEsV1EsRefreshLeftCrudListener",
            "RightEsV1EsRefreshLeftRightCrudCrudListener",
            "RightEsV1EsRefreshRightCrudListener"
        ),
        SearchableDtoEsTestSupport.spec.esDto to listOf(
            "ChildEsV1EsRefreshChildCrudListener",
            "ChildEsV1EsRefreshParentCrudListener",
            "ChildEsV2EsRefreshChildCrudListener",
            "ChildEsV2EsRefreshParentCrudListener"
        )
    )


    @Test
    fun `one listener per auto-refresh version and listenable dependency`(@TempDir dir: File) {

        expectedListeners.entries.forEachIndexed { index, (dto, expectedNames) ->
            assertThat(renderAll(dto, dir.resolve("dto$index")).keys).containsExactlyElementsOf(expectedNames.sorted())
        }

    }


    @Test
    fun `listener is a spring service implementing the entity crud listener with the version's index service`(@TempDir dir: File) {

        val sources = renderAll(spec.orderDto, dir)

        val customerV1 = sources.getValue("OrderEsV1EsRefreshCustomerCrudListener")

        assertThat(customerV1).contains("package com.example\n")
        assertThat(customerV1).contains("import org.maiaframework.elasticsearch.refresh.EsRefreshCollector\n")
        assertThat(customerV1).contains("import org.springframework.stereotype.Service\n")
        assertThat(customerV1).contains(
            "@Service\n" +
                "class OrderEsV1EsRefreshCustomerCrudListener(\n" +
                "    private val collector: EsRefreshCollector,\n" +
                "    private val indexService: OrderEsV1IndexService,\n" +
                "    private val repo: OrderEsDtoRepo\n" +
                ") : CustomerCrudListener {\n"
        )

        assertThat(sources.getValue("OrderEsV2EsRefreshCustomerCrudListener")).contains(
            "@Service\n" +
                "class OrderEsV2EsRefreshCustomerCrudListener(\n" +
                "    private val collector: EsRefreshCollector,\n" +
                "    private val indexService: OrderEsV2IndexService,\n" +
                "    private val repo: OrderEsDtoRepo\n" +
                ") : CustomerCrudListener {\n"
        )

        // the root listener needs no repo
        assertThat(sources.getValue("OrderEsV1EsRefreshOrderCrudListener")).contains(
            "@Service\n" +
                "class OrderEsV1EsRefreshOrderCrudListener(\n" +
                "    private val collector: EsRefreshCollector,\n" +
                "    private val indexService: OrderEsV1IndexService\n" +
                ") : OrderCrudListener {\n"
        )

    }


    @Test
    fun `listener class kdoc explains triggers, reconciliation and the two-phase membership rule`(@TempDir dir: File) {

        val source = renderAll(spec.leftDto, dir).getValue("LeftEsV1EsRefreshRightCrudListener")
        val kdoc = source.substringBefore("@Service\n")

        assertThat(kdoc).contains("/**")
        assertThat(kdoc).contains("[LeftEsV1IndexService]")
        assertThat(kdoc).contains("refreshIndex")
        assertThat(kdoc).contains("importers")
        assertThat(kdoc).contains("after commit")

    }


    @Test
    fun `root without many-to-many refreshes its own id and diffs its watched fields`(@TempDir dir: File) {

        val source = renderAll(spec.orderDto, dir).getValue("OrderEsV1EsRefreshOrderCrudListener")

        assertThat(functionText(source, "    override fun onOrderEntityCreated(")).isEqualTo(
            function(
                "override fun onOrderEntityCreated(entity: OrderEntity)",
                "        this.collector.enlistRefresh(this.indexService, listOf(entity.id))"
            )
        )

        assertThat(functionText(source, "    override fun onOrderEntityUpdated(")).isEqualTo(
            function(
                "override fun onOrderEntityUpdated(before: OrderEntity, after: OrderEntity)",
                "        if (before.customer != after.customer || before.orderNo != after.orderNo) {",
                "            this.collector.enlistRefresh(this.indexService, listOf(after.id))",
                "        }"
            )
        )

        assertThat(source).contains("\n    override fun onOrderEntityBeforeDelete(entity: OrderEntity) {}\n")

        assertThat(functionText(source, "    override fun onOrderEntityDeleted(")).isEqualTo(
            function(
                "override fun onOrderEntityDeleted(entity: OrderEntity)",
                "        this.collector.enlistRefresh(this.indexService, listOf(entity.id))"
            )
        )

        assertThat(source).doesNotContain("notShown")

    }


    @Test
    fun `root dto selecting only its id has nothing to diff on update`(@TempDir dir: File) {

        val source = renderAll(spec.orderIdOnlyDto, dir).getValue("OrderIdOnlyEsV1EsRefreshOrderCrudListener")

        assertThat(source).contains("\n    override fun onOrderEntityUpdated(before: OrderEntity, after: OrderEntity) {}\n")
        assertThat(functionText(source, "    override fun onOrderEntityCreated(")).isEqualTo(
            function(
                "override fun onOrderEntityCreated(entity: OrderEntity)",
                "        this.collector.enlistRefresh(this.indexService, listOf(entity.id))"
            )
        )

    }


    @Test
    fun `root with a many-to-many refreshes its own id on any update`(@TempDir dir: File) {

        val source = renderAll(spec.leftDto, dir).getValue("LeftEsV1EsRefreshLeftCrudListener")

        assertThat(functionText(source, "    override fun onLeftEntityUpdated(")).isEqualTo(
            function(
                "override fun onLeftEntityUpdated(before: LeftEntity, after: LeftEntity)",
                "        this.collector.enlistRefresh(this.indexService, listOf(after.id))"
            )
        )

        assertThat(functionText(source, "    override fun onLeftEntityCreated(")).isEqualTo(
            function(
                "override fun onLeftEntityCreated(entity: LeftEntity)",
                "        this.collector.enlistRefresh(this.indexService, listOf(entity.id))"
            )
        )

    }


    @Test
    fun `path entity resolves roots on watched change and before delete`(@TempDir dir: File) {

        val sources = renderAll(spec.orderDto, dir)
        val customer = sources.getValue("OrderEsV1EsRefreshCustomerCrudListener")

        assertThat(customer).contains("\n    override fun onCustomerEntityCreated(entity: CustomerEntity) {}\n")

        assertThat(functionText(customer, "    override fun onCustomerEntityUpdated(")).isEqualTo(
            function(
                "override fun onCustomerEntityUpdated(before: CustomerEntity, after: CustomerEntity)",
                "        if (before.name != after.name || before.region != after.region) {",
                "            val afterId = after.id",
                "            this.collector.enlistRefreshResolved(this.indexService) { this.repo.findRootIdsViaCustomer(afterId) }",
                "        }"
            )
        )

        assertThat(functionText(customer, "    override fun onCustomerEntityBeforeDelete(")).isEqualTo(
            function(
                "override fun onCustomerEntityBeforeDelete(entity: CustomerEntity)",
                "        this.collector.enlistRefreshNow(this.indexService) { this.repo.findRootIdsViaCustomer(entity.id) }"
            )
        )

        assertThat(customer).contains("\n    override fun onCustomerEntityDeleted(entity: CustomerEntity) {}\n")
        assertThat(customer).doesNotContain("notShown")

        val region = sources.getValue("OrderEsV1EsRefreshRegionCrudListener")

        assertThat(functionText(region, "    override fun onRegionEntityUpdated(")).isEqualTo(
            function(
                "override fun onRegionEntityUpdated(before: RegionEntity, after: RegionEntity)",
                "        if (before.name != after.name) {",
                "            val afterId = after.id",
                "            this.collector.enlistRefreshResolved(this.indexService) { this.repo.findRootIdsViaRegion(afterId) }",
                "        }"
            )
        )

    }


    @Test
    fun `many-to-many other side resolves old roots now and new roots after commit on every update`(@TempDir dir: File) {

        val source = renderAll(spec.leftDto, dir).getValue("LeftEsV1EsRefreshRightCrudListener")

        assertThat(source).contains("\n    override fun onRightEntityCreated(entity: RightEntity) {}\n")

        assertThat(functionText(source, "    override fun onRightEntityUpdated(")).isEqualTo(
            function(
                "override fun onRightEntityUpdated(before: RightEntity, after: RightEntity)",
                "        this.collector.enlistRefreshNow(this.indexService) { this.repo.findRootIdsViaRight(before.id) }",
                "        val afterId = after.id",
                "        this.collector.enlistRefreshResolved(this.indexService) { this.repo.findRootIdsViaRight(afterId) }"
            )
        )

        assertThat(functionText(source, "    override fun onRightEntityBeforeDelete(")).isEqualTo(
            function(
                "override fun onRightEntityBeforeDelete(entity: RightEntity)",
                "        this.collector.enlistRefreshNow(this.indexService) { this.repo.findRootIdsViaRight(entity.id) }"
            )
        )

        assertThat(source).contains("\n    override fun onRightEntityDeleted(entity: RightEntity) {}\n")

    }


    @Test
    fun `many-to-many join entity reads the root id straight from its foreign key to the root`(@TempDir dir: File) {

        val source = renderAll(spec.leftDto, dir).getValue("LeftEsV1EsRefreshLeftRightCrudCrudListener")

        assertThat(source).contains(
            "@Service\n" +
                "class LeftEsV1EsRefreshLeftRightCrudCrudListener(\n" +
                "    private val collector: EsRefreshCollector,\n" +
                "    private val indexService: LeftEsV1IndexService\n" +
                ") : LeftRightCrudCrudListener {\n"
        )
        assertThat(source).doesNotContain("repo")
        assertThat(source).doesNotContain("findRootIdsVia")
        assertThat(source.substringBefore("@Service\n")).contains("foreign key")

        assertThat(functionText(source, "    override fun onLeftRightCrudEntityCreated(")).isEqualTo(
            function(
                "override fun onLeftRightCrudEntityCreated(entity: LeftRightCrudEntity)",
                "        this.collector.enlistRefresh(this.indexService, listOf(entity.left))"
            )
        )

        assertThat(functionText(source, "    override fun onLeftRightCrudEntityUpdated(")).isEqualTo(
            function(
                "override fun onLeftRightCrudEntityUpdated(before: LeftRightCrudEntity, after: LeftRightCrudEntity)",
                "        if (before.left != after.left || before.right != after.right || before.weight != after.weight) {",
                "            this.collector.enlistRefresh(this.indexService, listOf(before.left, after.left).distinct())",
                "        }"
            )
        )

        assertThat(source).contains("\n    override fun onLeftRightCrudEntityBeforeDelete(entity: LeftRightCrudEntity) {}\n")

        assertThat(functionText(source, "    override fun onLeftRightCrudEntityDeleted(")).isEqualTo(
            function(
                "override fun onLeftRightCrudEntityDeleted(entity: LeftRightCrudEntity)",
                "        this.collector.enlistRefresh(this.indexService, listOf(entity.left))"
            )
        )

    }


    @Test
    fun `join entity listener uses the foreign key to the root, not the other side`(@TempDir dir: File) {

        val source = renderAll(spec.rightDto, dir).getValue("RightEsV1EsRefreshLeftRightCrudCrudListener")

        assertThat(functionText(source, "    override fun onLeftRightCrudEntityCreated(")).isEqualTo(
            function(
                "override fun onLeftRightCrudEntityCreated(entity: LeftRightCrudEntity)",
                "        this.collector.enlistRefresh(this.indexService, listOf(entity.right))"
            )
        )

    }


    @Test
    fun `each listener overrides exactly the members of its entity's crud listener interface`(@TempDir dir: File) {

        var checked = 0

        expectedListeners.keys.forEachIndexed { dtoIndex, dto ->

            EsRefreshListeners.renderersFor(dto).forEachIndexed { index, renderer ->

                val listenerDir = dir.resolve("listener-$dtoIndex-$index")
                renderer.renderToDir(listenerDir)
                val listenerSource = SearchableDtoEsTestSupport.singleKotlinFileIn(listenerDir)

                val overrides = overrideSignatures(listenerSource)

                assertThat(overrides)
                    .withFailMessage("%s overrides %s", renderer.classDef.uqcn, overrides)
                    .isEqualTo(interfaceSignatures(renderer.dependency.entityDef, dir.resolve("interface-$dtoIndex-$index")))

                checked++

            }

        }

        assertThat(checked).isEqualTo(expectedListeners.values.sumOf { it.size })

    }


    @Test
    fun `dto whose versions are all autoRefresh false gets no listeners and never validates`(@TempDir dir: File) {

        val noAutoRefreshSpec = EsRefreshListenerTestSpec(autoRefresh = false)

        assertThat(EsRefreshListeners.renderersFor(noAutoRefreshSpec.orderDto)).isEmpty()
        assertThat(renderAll(noAutoRefreshSpec.orderDto, dir)).isEmpty()

        val noCrudSpec = NoCrudSpec()

        assertThat(EsRefreshListeners.renderersFor(noCrudSpec.noAutoRefreshDto)).isEmpty()
        assertThat(EsRefreshListeners.renderersFor(noCrudSpec.plainDto)).isEmpty()

    }


    @Test
    fun `root without a crud listener fails validation`() {

        assertThatThrownBy { EsRefreshListeners.renderersFor(NoCrudSpec().autoRefreshDto) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("ChildEs")
            .hasMessageContaining("Child")
            .hasMessageContaining("to have a CrudListener")

    }


    @Test
    fun `rendering is deterministic`(@TempDir dir: File) {

        listOf(spec.orderDto, spec.leftDto).forEachIndexed { index, dto ->

            val first = renderAll(dto, dir.resolve("a$index"))
            val second = renderAll(dto, dir.resolve("b$index"))
            val otherSpec = EsRefreshListenerTestSpec()
            val third = renderAll(listOf(otherSpec.orderDto, otherSpec.leftDto)[index], dir.resolve("c$index"))

            assertThat(second).isEqualTo(first)
            assertThat(third).isEqualTo(first)

        }

    }


    /** Entities without crud {}: reading esRefreshDependencies fails validation. */
    private class NoCrudSpec : AbstractSpec(AppKey("Test")) {

        val parent = entity("com.example", "Parent") {
            field("name", FieldTypes.string)
        }

        val child = entity("com.example", "Child") {
            field("title", FieldTypes.string)
            foreignKey("parent", parent)
        }

        val autoRefreshDto = searchableDto(baseName = "ChildEs", entityDef = child) {
            elasticsearchEnabled()
            field("title")
        }

        val noAutoRefreshDto = searchableDto(baseName = "ChildNoAutoEs", entityDef = child) {
            elasticsearchEnabled(autoRefresh = false)
            field("title")
            field("parentName", "parent.name")
        }

        val plainDto = searchableDto(baseName = "ChildPlain", entityDef = child) {
            field("title")
            field("parentName", "parent.name")
        }

    }


}
