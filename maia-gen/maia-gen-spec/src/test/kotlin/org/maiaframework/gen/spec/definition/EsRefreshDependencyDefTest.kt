package org.maiaframework.gen.spec.definition

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes


class EsRefreshDependencyDefTest {


    private class DependencySpec : AbstractSpec(AppKey("Test")) {

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
            field("notShown", FieldTypes.string)
            foreignKey("customer", customer)
            crud {}
        }

        val orderDto = searchableDto(baseName = "OrderEs", entityDef = order) {
            elasticsearchEnabled()
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
            field("shippingRegionName", "shippingCustomer.region.name")
        }

        val left = entity("com.example", "Left", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
            crud {}
        }

        val right = entity("com.example", "Right", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
            field("notInPkAndName", FieldTypes.int)
            crud {}
        }

        val leftRight = manyToManyEntity(
            "com.example",
            "LeftRight",
            leftEntity = ReferencedEntity("left", "Left", left, IsEditableByUser.TRUE),
            rightEntity = ReferencedEntity("right", "Right", right, IsEditableByUser.TRUE)
        ) {
            field("weight", FieldTypes.int)
            field_lastModifiedTimestamp()
            crud {}
        }

        val leftDto = searchableDto(baseName = "LeftEs", entityDef = left) {
            elasticsearchEnabled()
            field("name")
            manyToManyField("rights", leftRight)
        }

        val leftJoinOnlyDto = searchableDto(baseName = "LeftJoinOnlyEs", entityDef = left) {
            elasticsearchEnabled()
            manyToManyJoin(leftRight)
            field("name")
        }

        val rightDto = searchableDto(baseName = "RightEs", entityDef = right) {
            elasticsearchEnabled()
            field("name")
            manyToManyField("lefts", leftRight)
        }

        val leftRightOther = manyToManyEntity(
            "com.example",
            "LeftRightOther",
            leftEntity = ReferencedEntity("left", "Left", left, IsEditableByUser.TRUE),
            rightEntity = ReferencedEntity("right", "Right", right, IsEditableByUser.TRUE)
        ) {
            crud {}
        }

        val leftTwoJoinsDto = searchableDto(baseName = "LeftTwoJoinsEs", entityDef = left) {
            elasticsearchEnabled()
            field("name")
            manyToManyField("rightsA", leftRight)
            manyToManyField("rightsB", leftRightOther)
        }

        val leftJoinQualifiedDto = searchableDto(baseName = "LeftJoinQualifiedEs", entityDef = left) {
            elasticsearchEnabled()
            manyToManyJoin(leftRight)
            field("name")
            field("rightName", "LeftRight.right.name")
        }

        val orderFkOnlyDto = searchableDto(baseName = "OrderFkOnlyEs", entityDef = order) {
            elasticsearchEnabled()
            field("customer")
            field("customerId", "customer.id")
        }

        fun orderWithUnrelatedJoin() = searchableDto(baseName = "OrderUnrelatedJoinEs", entityDef = order) {
            elasticsearchEnabled()
            manyToManyJoin(leftRight)
            field("orderNo")
        }

        val compositeTarget = entity("com.example", "CompositeTarget") {
            field("a", FieldTypes.string) { primaryKey() }
            field("b", FieldTypes.int) { primaryKey() }
            field("label", FieldTypes.string)
            crud {}
        }

        val compositeHolder = entity("com.example", "CompositeHolder") {
            foreignKey("target", compositeTarget)
            crud {}
        }

        val compositeHolderDto = searchableDto(baseName = "CompositeHolderEs", entityDef = compositeHolder) {
            elasticsearchEnabled()
            field("targetLabel", "target.label")
        }

        val party = entity("com.example", "Party") {
            isAbstract = true
            field("displayName", FieldTypes.string)
            crud {}
        }

        val org = entity("com.example", "Org") {
            superclass(party)
            typeDiscriminator("ORG")
            field("orgName", FieldTypes.string)
            crud {}
        }

        val contract = entity("com.example", "Contract") {
            foreignKey("org", org)
            crud {}
        }

        val contractDto = searchableDto(baseName = "ContractEs", entityDef = contract) {
            elasticsearchEnabled()
            field("orgDisplayName", "org.displayName")
            field("orgName", "org.orgName")
        }

    }


    private class NoListenerSpec : AbstractSpec(AppKey("Test")) {

        val region = entity("com.example", "Region") {
            field("name", FieldTypes.string)
        }

        val customer = entity("com.example", "Customer") {
            field("name", FieldTypes.string)
            foreignKey("region", region)
            crud {}
        }

        val enabled = searchableDto(baseName = "CustomerEs", entityDef = customer) {
            elasticsearchEnabled()
            field("name")
            field("regionName", "region.name")
        }

        val plain = searchableDto(baseName = "RegionPlain", entityDef = region) {
            field("name")
        }

        val plainJoin = searchableDto(baseName = "CustomerPlain", entityDef = customer) {
            field("regionName", "region.name")
        }

    }


    private class SelfManyToManySpec : AbstractSpec(AppKey("Test")) {

        val person = entity("com.example", "Person", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
            crud {}
        }

        val friendship = manyToManyEntity(
            "com.example",
            "Friendship",
            leftEntity = ReferencedEntity("friendA", "Friend A", person),
            rightEntity = ReferencedEntity("friendB", "Friend B", person)
        ) {
            crud {}
        }

        val personDto = searchableDto(baseName = "PersonEs", entityDef = person) {
            elasticsearchEnabled()
            field("name")
            manyToManyField("friends", friendship)
        }

        val personJoinOnlyDto = searchableDto(baseName = "PersonJoinOnlyEs", entityDef = person) {
            elasticsearchEnabled()
            manyToManyJoin(friendship)
            field("name")
        }

    }


    private class ShortcodeSpec : AbstractSpec(AppKey("Test")) {

        val item = entity("com.example", "Item", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
            crud {}
        }

        val tag = entity("com.example", "Tag", nameFieldForPkAndNameDto = "name", shortcodeFieldForPkAndNameDto = "code") {
            field("name", FieldTypes.string)
            field("code", FieldTypes.string)
            field("notInPkAndName", FieldTypes.int)
            crud {}
        }

        val itemTag = simpleManyToManyEntity(
            "com.example",
            "ItemTag",
            leftEntity = ReferencedEntity("item", "Item", item),
            rightEntity = ReferencedEntity("tag", "Tag", tag)
        )

        val itemDto = searchableDto(baseName = "ItemEs", entityDef = item) {
            elasticsearchEnabled()
            field("name")
            manyToManyField("tags", itemTag)
        }

    }


    private class JoinWithoutListenerSpec : AbstractSpec(AppKey("Test")) {

        val left = entity("com.example", "Left", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
            crud {}
        }

        val right = entity("com.example", "Right", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
            crud {}
        }

        val join = simpleManyToManyEntity(
            "com.example",
            "LeftRightSimple",
            leftEntity = ReferencedEntity("left", "Left", left, IsEditableByUser.TRUE),
            rightEntity = ReferencedEntity("right", "Right", right, IsEditableByUser.TRUE)
        )

        val leftDto = searchableDto(baseName = "LeftSimpleEs", entityDef = left) {
            elasticsearchEnabled()
            field("name")
            manyToManyField("rights", join)
        }

    }


    private class NonDomainIdPkSpec : AbstractSpec(AppKey("Test")) {

        val country = entity("com.example", "Country") {
            field("code", FieldTypes.string) { primaryKey() }
            field("name", FieldTypes.string)
            crud {}
        }

        val address = entity("com.example", "Address") {
            field("street", FieldTypes.string)
            foreignKey("country", country)
            crud {}
        }

        val addressDto = searchableDto(baseName = "AddressEs", entityDef = address) {
            elasticsearchEnabled()
            field("street")
            field("countryName", "country.name")
        }

        val owner = entity("com.example", "Owner", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
            crud {}
        }

        val label = entity("com.example", "Label", nameFieldForPkAndNameDto = "name") {
            field("code", FieldTypes.string) { primaryKey() }
            field("name", FieldTypes.string)
            crud {}
        }

        val ownerLabel = simpleManyToManyEntity(
            "com.example",
            "OwnerLabel",
            leftEntity = ReferencedEntity("owner", "Owner", owner, IsEditableByUser.TRUE),
            rightEntity = ReferencedEntity("label", "Label", label, IsEditableByUser.TRUE)
        )

        val ownerDto = searchableDto(baseName = "OwnerEs", entityDef = owner) {
            elasticsearchEnabled()
            field("name")
            manyToManyField("labels", ownerLabel)
        }

        val ownerCoded = manyToManyEntity(
            "com.example",
            "OwnerCoded",
            leftEntity = ReferencedEntity("owner", "Owner", owner, IsEditableByUser.TRUE),
            rightEntity = ReferencedEntity("label", "Label", label, IsEditableByUser.TRUE)
        ) {
            field("code", FieldTypes.string) { primaryKey() }
            crud {}
        }

        val ownerJoinOnlyDto = searchableDto(baseName = "OwnerJoinOnlyEs", entityDef = owner) {
            elasticsearchEnabled()
            manyToManyJoin(ownerCoded)
            field("name")
        }

    }


    private val spec = DependencySpec()


    private fun EsRefreshDependencyDef.watched(): Set<String> = this.watchedFields.map { it.value }.toSet()


    private fun JoinHop.describe(): String =
        "${fromEntity.entityBaseName}.${fromField.classFieldName}->${toEntity.entityBaseName}.${toField.classFieldName}"


    private fun EsRefreshDependencyDef.describePaths(): List<List<String>> = this.pathsFromRoot.map { path -> path.map { it.describe() } }


    private fun SearchableDtoDef.describe(): List<String> = this.esRefreshDependencies.map { dep ->
        "${dep.entityDef.entityBaseName} root=${dep.isRoot} watched=${dep.watchedFields.map { it.value }} paths=${dep.describePaths()} any=${dep.refreshOnAnyUpdate} m2m=${dep.isManyToManyMembership} listener=${dep.hasCrudListener}"
    }


    @Test
    fun `chain of foreign keys yields root, intermediate and leaf dependencies`() {

        val deps = spec.orderDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Order", "Customer", "Region")

        val (orderDep, customerDep, regionDep) = deps

        assertThat(orderDep.isRoot).isTrue()
        assertThat(orderDep.watched()).containsExactlyInAnyOrder("orderNo", "customer")
        assertThat(orderDep.pathsFromRoot).containsExactly(emptyList())
        assertThat(orderDep.refreshOnAnyUpdate).isFalse()
        assertThat(orderDep.isManyToManyMembership).isFalse()

        assertThat(customerDep.isRoot).isFalse()
        assertThat(customerDep.watched()).containsExactlyInAnyOrder("name", "region")
        assertThat(customerDep.describePaths()).containsExactly(listOf("Order.customer->Customer.id"))
        assertThat(customerDep.refreshOnAnyUpdate).isFalse()
        assertThat(customerDep.isManyToManyMembership).isFalse()

        assertThat(regionDep.watched()).containsExactly("name")
        assertThat(regionDep.describePaths()).containsExactly(listOf("Order.customer->Customer.id", "Customer.region->Region.id"))

        val hop = regionDep.pathsFromRoot.single()[1]
        assertThat(hop.fromEntity).isSameAs(spec.customer)
        assertThat(hop.fromField.classFieldName.value).isEqualTo("region")
        assertThat(hop.toEntity).isSameAs(spec.region)
        assertThat(hop.toField.classFieldName.value).isEqualTo("id")

    }


    @Test
    fun `two foreign keys to the same entity yield one dependency with two paths`() {

        val deps = spec.shipmentDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Shipment", "Customer", "Region")

        val (shipmentDep, customerDep, regionDep) = deps

        assertThat(shipmentDep.watched()).containsExactlyInAnyOrder("billingCustomer", "shippingCustomer")

        assertThat(customerDep.watched()).containsExactlyInAnyOrder("name", "region")
        assertThat(customerDep.describePaths()).containsExactly(
            listOf("Shipment.billingCustomer->Customer.id"),
            listOf("Shipment.shippingCustomer->Customer.id")
        )

        assertThat(regionDep.describePaths()).containsExactly(
            listOf("Shipment.shippingCustomer->Customer.id", "Customer.region->Region.id")
        )

    }


    @Test
    fun `many-to-many field yields join entity and other side dependencies`() {

        val deps = spec.leftDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Left", "LeftRight", "Right")

        val (leftDep, joinDep, rightDep) = deps

        assertThat(leftDep.isRoot).isTrue()
        assertThat(leftDep.watched()).containsExactly("name")
        assertThat(leftDep.pathsFromRoot).containsExactly(emptyList())
        assertThat(leftDep.refreshOnAnyUpdate).isTrue()
        assertThat(leftDep.isManyToManyMembership).isFalse()

        assertThat(joinDep.isRoot).isFalse()
        assertThat(joinDep.isManyToManyMembership).isTrue()
        assertThat(joinDep.watched()).containsExactlyInAnyOrder("left", "right", "weight")
        assertThat(joinDep.describePaths()).containsExactly(listOf("Left.id->LeftRight.left"))

        assertThat(joinDep.refreshOnAnyUpdate).isFalse()
        assertThat(joinDep.hasCrudListener).isTrue()

        assertThat(rightDep.isManyToManyMembership).isTrue()
        assertThat(rightDep.refreshOnAnyUpdate).isTrue()
        assertThat(rightDep.hasCrudListener).isTrue()
        assertThat(rightDep.watched()).containsExactly("name")
        assertThat(rightDep.describePaths()).containsExactly(listOf("Left.id->LeftRight.left", "LeftRight.right->Right.id"))

    }


    @Test
    fun `right-rooted many-to-many walks from the right foreign key to the left entity`() {

        val deps = spec.rightDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Right", "LeftRight", "Left")

        val (rightDep, joinDep, leftDep) = deps

        assertThat(rightDep.isRoot).isTrue()
        assertThat(rightDep.refreshOnAnyUpdate).isTrue()

        assertThat(joinDep.describePaths()).containsExactly(listOf("Right.id->LeftRight.right"))

        assertThat(leftDep.entityDef).isSameAs(spec.left)
        assertThat(leftDep.isManyToManyMembership).isTrue()
        assertThat(leftDep.refreshOnAnyUpdate).isTrue()
        assertThat(leftDep.describePaths()).containsExactly(listOf("Right.id->LeftRight.right", "LeftRight.left->Left.id"))

    }


    @Test
    fun `two many-to-many fields to the same other side yield one other-side dependency with two paths`() {

        val deps = spec.leftTwoJoinsDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Left", "LeftRight", "Right", "LeftRightOther")

        val rightDep = deps[2]

        assertThat(rightDep.describePaths()).containsExactly(
            listOf("Left.id->LeftRight.left", "LeftRight.right->Right.id"),
            listOf("Left.id->LeftRightOther.left", "LeftRightOther.right->Right.id")
        )

        assertThat(deps[1].describePaths()).containsExactly(listOf("Left.id->LeftRight.left"))
        assertThat(deps[3].describePaths()).containsExactly(listOf("Left.id->LeftRightOther.left"))

        assertThat(DependencySpec().leftTwoJoinsDto.describe()).isEqualTo(spec.leftTwoJoinsDto.describe())

    }


    @Test
    fun `join-qualified field path marks the other side as membership`() {

        val deps = spec.leftJoinQualifiedDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Left", "LeftRight", "Right")

        val (leftDep, joinDep, rightDep) = deps

        assertThat(leftDep.refreshOnAnyUpdate).isTrue()
        assertThat(leftDep.watched()).containsExactly("name")

        assertThat(joinDep.isManyToManyMembership).isTrue()
        assertThat(joinDep.refreshOnAnyUpdate).isFalse()
        assertThat(joinDep.describePaths()).containsExactly(listOf("Left.id->LeftRight.left"))

        assertThat(rightDep.isManyToManyMembership).isTrue()
        assertThat(rightDep.refreshOnAnyUpdate).isTrue()
        assertThat(rightDep.watched()).containsExactly("name")
        assertThat(rightDep.describePaths()).containsExactly(listOf("Left.id->LeftRight.left", "LeftRight.right->Right.id"))

    }


    @Test
    fun `pkAndName shortcode on the other side is watched`() {

        val deps = ShortcodeSpec().itemDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Item", "ItemTag", "Tag")
        assertThat(deps[2].watched()).containsExactlyInAnyOrder("name", "code")

    }


    @Test
    fun `foreign key field and foreign primary key yield no foreign entity dependency`() {

        val deps = spec.orderFkOnlyDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Order")
        assertThat(deps[0].watched()).containsExactly("customer")

    }


    @Test
    fun `join entity with no foreign key to the root is rejected`() {

        assertThatThrownBy { spec.orderWithUnrelatedJoin().esRefreshDependencies }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("LeftRight")
            .hasMessageContaining("no foreign key to the root entity Order")
            .hasMessageContaining("OrderUnrelatedJoinEs")

    }


    @Test
    fun `join entity with several foreign keys to the root is rejected with a distinct message`() {

        assertThatThrownBy { SelfManyToManySpec().personJoinOnlyDto.esRefreshDependencies }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Friendship has more than one foreign key to the root entity Person")
            .hasMessageContaining("PersonJoinOnlyEs")
            .hasMessageNotContaining("self many-to-many")

    }


    @Test
    fun `composite primary key path entity is rejected`() {

        assertThatThrownBy { spec.compositeHolderDto.esRefreshDependencies }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("CompositeTarget")
            .hasMessageContaining("single primary key")
            .hasMessageContaining("CompositeHolderEs")

    }


    @Test
    fun `each dependency has exactly one role`() {

        assertThat(spec.orderDto.esRefreshDependencies.map { "${it.entityDef.entityBaseName}=${it.role}" })
            .containsExactly("Order=ROOT", "Customer=PATH", "Region=PATH")

        assertThat(spec.leftDto.esRefreshDependencies.map { "${it.entityDef.entityBaseName}=${it.role}" })
            .containsExactly("Left=ROOT", "LeftRight=CHILD", "Right=OTHER_SIDE")

        assertThat(spec.leftJoinQualifiedDto.esRefreshDependencies.map { "${it.entityDef.entityBaseName}=${it.role}" })
            .containsExactly("Left=ROOT", "LeftRight=CHILD", "Right=OTHER_SIDE")

    }


    @Test
    fun `path entity without a DomainId primary key is rejected`() {

        assertThatThrownBy { NonDomainIdPkSpec().addressDto.esRefreshDependencies }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Country")
            .hasMessageContaining("AddressEs")
            .hasMessageContaining("requires a single DomainId primary key")

    }


    @Test
    fun `many-to-many other side without a DomainId primary key is rejected`() {

        assertThatThrownBy { NonDomainIdPkSpec().ownerDto.esRefreshDependencies }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Label")
            .hasMessageContaining("OwnerEs")
            .hasMessageContaining("requires a single DomainId primary key")

    }


    @Test
    fun `join entity without a DomainId primary key is exempt from validation`() {

        val deps = NonDomainIdPkSpec().ownerJoinOnlyDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Owner", "OwnerCoded")
        assertThat(deps[1].entityDef.primaryKeyFields.map { it.classFieldName.value }).containsExactly("code")

    }


    @Test
    fun `join entity without crud listener is exempt from validation`() {

        val joinSpec = JoinWithoutListenerSpec()

        val deps = joinSpec.leftDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Left", "LeftRightSimple", "Right")
        assertThat(deps[0].hasCrudListener).isTrue()
        assertThat(deps[1].isManyToManyMembership).isTrue()
        assertThat(deps[1].hasCrudListener).isFalse()
        assertThat(deps[2].hasCrudListener).isTrue()
        assertThat(deps[2].refreshOnAnyUpdate).isTrue()

    }


    @Test
    fun `bare many-to-many join yields a join entity dependency only`() {

        val deps = spec.leftJoinOnlyDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Left", "LeftRight")
        assertThat(deps[0].refreshOnAnyUpdate).isTrue()
        assertThat(deps[1].isManyToManyMembership).isTrue()
        assertThat(deps[1].describePaths()).containsExactly(listOf("Left.id->LeftRight.left"))

    }


    @Test
    fun `self many-to-many is rejected`() {

        val selfSpec = SelfManyToManySpec()

        assertThatThrownBy { selfSpec.personDto.esRefreshDependencies }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("self many-to-many not supported")
            .hasMessageContaining("Friendship")
            .hasMessageContaining("PersonEs")

    }


    @Test
    fun `entity without crud listener in a path is rejected`() {

        val noListenerSpec = NoListenerSpec()

        assertThatThrownBy { noListenerSpec.enabled.esRefreshDependencies }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Region")
            .hasMessageContaining("CustomerEs")

    }


    @Test
    fun `dto without elasticsearch has no dependencies and never validates`() {

        val noListenerSpec = NoListenerSpec()

        assertThat(noListenerSpec.plain.esRefreshDependencies).isEmpty()
        assertThat(noListenerSpec.plainJoin.esRefreshDependencies).isEmpty()

    }


    @Test
    fun `field inherited from a superclass is attributed to the entity in the path`() {

        val deps = spec.contractDto.esRefreshDependencies

        assertThat(deps.map { it.entityDef.entityBaseName.value }).containsExactly("Contract", "Org")
        assertThat(deps[1].entityDef).isSameAs(spec.org)
        assertThat(deps[1].watched()).containsExactlyInAnyOrder("displayName", "orgName")
        assertThat(deps[1].describePaths()).containsExactly(listOf("Contract.org->Org.id"))

    }


    @Test
    fun `dependencies and paths are deterministic across builds`() {

        val other = DependencySpec()

        assertThat(other.orderDto.describe()).isEqualTo(spec.orderDto.describe())
        assertThat(other.shipmentDto.describe()).isEqualTo(spec.shipmentDto.describe())
        assertThat(other.leftDto.describe()).isEqualTo(spec.leftDto.describe())
        assertThat(other.contractDto.describe()).isEqualTo(spec.contractDto.describe())

    }


}
