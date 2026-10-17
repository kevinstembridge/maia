package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ApplicationModelDef
import org.maiaframework.gen.spec.definition.EsRefreshRole
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File


/** Hand-built EsDocs whose sources are reached through foreign keys (`watchesVia`). */
class HandBuiltEsDocViaRendererTest {


    private class OrderSpec(path: String = "customer.region") : AbstractSpec(AppKey("Test")) {

        val region = entity("com.example.order", "Region", nameFieldForPkAndNameDto = "name", deletable = Deletable.TRUE) {
            field("name", FieldTypes.string) {
                fieldDisplayName("Name")
                editableByUser()
            }
            crud {
                create { api {} }
                update { api {} }
                delete { api {} }
            }
        }

        val customer = entity("com.example.order", "Customer", nameFieldForPkAndNameDto = "name", deletable = Deletable.TRUE) {
            field("name", FieldTypes.string) {
                fieldDisplayName("Name")
                editableByUser()
            }
            foreignKey("region", region, isEditableByUser = IsEditableByUser.TRUE, fieldDisplayName = "Region")
            crud {
                create { api {} }
                update { api {} }
                delete { api {} }
            }
        }

        val order = entity("com.example.order", "Order", nameFieldForPkAndNameDto = "orderNo", deletable = Deletable.TRUE) {
            field("orderNo", FieldTypes.string) {
                fieldDisplayName("Order no")
                editableByUser()
            }
            foreignKey("customer", customer, isEditableByUser = IsEditableByUser.TRUE, fieldDisplayName = "Customer")
            crud {
                create { api {} }
                update { api {} }
                delete { api {} }
            }
        }

        val carrier = entity("com.example.order", "Carrier", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string) {
                fieldDisplayName("Name")
                editableByUser()
            }
            crud {
                create { api {} }
                update { api {} }
            }
        }

        val shipment = entity("com.example.order", "Shipment") {
            foreignKey("order", order, isEditableByUser = IsEditableByUser.TRUE, fieldDisplayName = "Order")
            foreignKey("carrier", carrier, isEditableByUser = IsEditableByUser.TRUE, fieldDisplayName = "Carrier")
            crud {
                create { api {} }
                update { api {} }
            }
        }

        val orderEsDoc = esDoc("com.example.order", "OrderSummary", indexDescription = "Order summary") {
            autoRefresh(true)
            sourceEntity(order)
            watchesVia(customer, "customer", "name")
            watchesVia(region, path)
            watchesVia(carrier, "name") {
                reverse(shipment, "order")
                forward("carrier")
            }
            field("id", FieldTypes.domainId) { isIdField() }
        }

    }


    private val spec = OrderSpec()


    private val applicationModelDef: ApplicationModelDef = object : ApplicationSpec("com.example") {
        override val modelDefs: List<ModelDef> = listOf(spec.modelDef)
    }.applicationModelDef


    @Test
    fun `via sources become PATH dependencies with their foreign key hops`() {

        val dependencies = spec.orderEsDoc.refreshDependencies.associateBy { it.entityDef.entityBaseName.value }

        assertThat(dependencies.keys).containsExactly("Order", "Customer", "Region", "Carrier")
        assertThat(dependencies.getValue("Region").role).isEqualTo(EsRefreshRole.PATH)
        assertThat(dependencies.getValue("Region").pathsFromRoot.single().map { "${it.fromEntity.entityBaseName}.${it.fromField.classFieldName}" })
            .containsExactly("Order.customer", "Customer.region")
        // defaults to every non-key, non-audit field
        assertThat(dependencies.getValue("Region").watchedFields.map { it.value }).containsExactly("name")

    }


    @Test
    fun `a path can step backwards through a child table`(@TempDir dir: File) {

        val carrier = spec.orderEsDoc.refreshDependencies.single { it.entityDef.entityBaseName.value == "Carrier" }

        assertThat(carrier.pathsFromRoot.single().map { "${it.fromEntity.entityBaseName}->${it.toEntity.entityBaseName}" })
            .containsExactly("Order->Shipment", "Shipment->Carrier")

        EsDocRefreshRepoRenderer(spec.orderEsDoc, applicationModelDef).renderToDir(dir)
        val query = SearchableDtoEsTestSupport.singleKotlinFileIn(dir).substringAfter("fun findRootIdsViaCarrier")

        assertThat(Regex("inner join").findAll(query).count()).isEqualTo(2)
        assertThat(query).contains("on h0.carrier_id = h1.id")
        assertThat(query).contains("where h1.id = :id")

    }


    @Test
    fun `a path that does not end at the declared entity is rejected`() {

        assertThatThrownBy { OrderSpec(path = "customer") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("ends at Customer, not Region")

    }


    @Test
    fun `renders the reverse queries into a refresh repo`(@TempDir dir: File) {

        EsDocRefreshRepoRenderer(spec.orderEsDoc, applicationModelDef).renderToDir(dir)
        val source = SearchableDtoEsTestSupport.singleKotlinFileIn(dir)

        assertThat(source).contains("class OrderSummaryV1EsRefreshRepo(")
        assertThat(source).contains("private val jdbcOps: JdbcOps")
        assertThat(source).contains("fun findRootIdsViaCustomer(id: DomainId): List<DomainId> {")
        assertThat(source).contains("fun findRootIdsViaRegion(id: DomainId): List<DomainId> {")

        val regionQuery = source.substringAfter("fun findRootIdsViaRegion")
        assertThat(regionQuery).contains("inner join ")
        assertThat(regionQuery).contains("on h0.")
        assertThat(regionQuery).contains("where h1.id = :id")

    }


    @Test
    fun `PATH listeners resolve the roots through the refresh repo`(@TempDir dir: File) {

        EsRefreshListeners.renderersFor(spec.orderEsDoc).forEach { it.renderToDir(dir) }

        val region = SearchableDtoEsTestSupport.kotlinFilesIn(dir).single { it.nameWithoutExtension == "OrderSummaryV1EsRefreshRegionCrudListener" }.readText()

        assertThat(region).contains("private val repo: OrderSummaryV1EsRefreshRepo")
        assertThat(region).contains("if (before.name != after.name) {")
        assertThat(region).contains("this.repo.findRootIdsViaRegion(afterId)")
        assertThat(region).contains("this.collector.enlistRefreshNow(this.indexService) { this.repo.findRootIdsViaRegion(entity.id) }")

    }


}
