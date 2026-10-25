package org.maiaframework.gen.docs

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.nio.file.Path
import kotlin.io.path.readText


class MultiSpecDomainDocsTest {


    class OrdersModelSpec : AbstractSpec(AppKey("MultiOrders")) {

        val order = entity("com.example.orders", "Order") {
            field("reference", FieldTypes.string) { lengthConstraint(max = 20) }
        }

    }


    class OrdersApplicationSpec : ApplicationSpec("com.example.orders") {

        override val modelDefs: List<ModelDef> = listOf(OrdersModelSpec().modelDef)

    }


    class BillingModelSpec : AbstractSpec(AppKey("MultiBilling")) {

        val invoice = entity("com.example.billing", "Invoice") {
            field("number", FieldTypes.string) { lengthConstraint(max = 20) }
        }

    }


    class BillingApplicationSpec : ApplicationSpec("com.example.billing") {

        override val modelDefs: List<ModelDef> = listOf(BillingModelSpec().modelDef)

    }


    class OtherOrderModelSpec : AbstractSpec(AppKey("MultiOtherOrder")) {

        val order = entity("com.other", "Order") {
            field("code", FieldTypes.string) { lengthConstraint(max = 20) }
        }

    }


    class OtherOrderApplicationSpec : ApplicationSpec("com.other") {

        override val modelDefs: List<ModelDef> = listOf(OtherOrderModelSpec().modelDef)

    }


    class SameTableModelSpec : AbstractSpec(AppKey("MultiOrders")) {

        val purchase = entity("com.other", "Purchase") {
            tableName("order")
            field("code", FieldTypes.string) { lengthConstraint(max = 20) }
        }

    }


    class SameTableApplicationSpec : ApplicationSpec("com.other") {

        override val modelDefs: List<ModelDef> = listOf(SameTableModelSpec().modelDef)

    }


    @TempDir
    lateinit var outputDir: Path


    @Test
    fun `index groups Entities by originating Spec and Entity pages show their Spec`() {

        generateDomainDocs(listOf(OrdersApplicationSpec(), BillingApplicationSpec()), outputDir)

        val index = outputDir.resolve("index.html").readText()
        val ordersHeading = index.indexOf("<h2>OrdersApplicationSpec</h2>")
        val billingHeading = index.indexOf("<h2>BillingApplicationSpec</h2>")
        val orderLink = index.indexOf("""<a href="entities/Order.html">""")
        val invoiceLink = index.indexOf("""<a href="entities/Invoice.html">""")
        assertThat(listOf(ordersHeading, billingHeading, orderLink, invoiceLink)).allMatch { it >= 0 }
        // each link sits after its own Spec heading and before the next heading
        val first = minOf(ordersHeading, billingHeading)
        val second = maxOf(ordersHeading, billingHeading)
        val (firstLink, secondLink) = if (ordersHeading < billingHeading) orderLink to invoiceLink else invoiceLink to orderLink
        assertThat(firstLink).isBetween(first, second)
        assertThat(secondLink).isGreaterThan(second)

        assertThat(outputDir.resolve("entities/Order.html").readText()).contains("OrdersApplicationSpec")
        assertThat(outputDir.resolve("entities/Invoice.html").readText()).contains("BillingApplicationSpec")

    }


    @Test
    fun `fails clearly when two Specs declare an Entity with the same name`() {

        assertThatThrownBy {
            generateDomainDocs(listOf(OrdersApplicationSpec(), OtherOrderApplicationSpec()), outputDir)
        }
            .hasMessageContaining("Order")
            .hasMessageContaining("OrdersApplicationSpec")
            .hasMessageContaining("OtherOrderApplicationSpec")

    }


    @Test
    fun `fails clearly when two Specs declare Entities backed by the same table`() {

        assertThatThrownBy {
            generateDomainDocs(listOf(OrdersApplicationSpec(), SameTableApplicationSpec()), outputDir)
        }
            .hasMessageContaining("table")
            .hasMessageContaining("Order")
            .hasMessageContaining("Purchase")

    }


    @Test
    fun `fails clearly when a Spec class cannot be loaded`() {

        assertThatThrownBy { runDomainDocs(listOf(OrdersApplicationSpec::class.java.name, "com.nope.MissingSpec"), outputDir) }
            .hasMessageContaining("com.nope.MissingSpec")

    }


    object FirstGroup {

        class SharedNameSpec : ApplicationSpec("com.example.first") {

            override val modelDefs: List<ModelDef> = listOf(OrdersModelSpec().modelDef)

        }

    }


    object SecondGroup {

        class SharedNameSpec : ApplicationSpec("com.example.second") {

            override val modelDefs: List<ModelDef> = listOf(BillingModelSpec().modelDef)

        }

    }


    @Test
    fun `Specs sharing a simple name are grouped separately and shown by qualified name`() {

        generateDomainDocs(listOf(FirstGroup.SharedNameSpec(), SecondGroup.SharedNameSpec()), outputDir)

        val index = outputDir.resolve("index.html").readText()
        assertThat(index)
            .contains("<h2>${FirstGroup.SharedNameSpec::class.java.name}</h2>")
            .contains("<h2>${SecondGroup.SharedNameSpec::class.java.name}</h2>")
        assertThat(outputDir.resolve("entities/Order.html").readText()).contains(FirstGroup.SharedNameSpec::class.java.name)

    }


    @Test
    fun `Specs with distinct simple names are shown by simple name`() {

        generateDomainDocs(listOf(OrdersApplicationSpec()), outputDir)

        assertThat(outputDir.resolve("index.html").readText()).contains("<h2>OrdersApplicationSpec</h2>")

    }


}
