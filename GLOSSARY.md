# Maia

A framework for building full-stack web applications on Kotlin, Spring Boot, Angular and PostgreSQL, centred on a code generator that produces every layer of an application from a single declarative model.

## Language

### Code generation

**Spec**:
A Kotlin class extending `AbstractSpec` that declares an application's model. The only hand-written input to the Generator.
_Avoid_: Schema, model file

**Generator**:
The tool that reads Specs and emits source code for every Layer.
_Avoid_: Scaffolder, compiler

**Layer**:
One stage of the generated application, in the order spec → domain → dao → repo → service → web → app, plus the Angular UI. Each Layer is its own Gradle module and the Generator emits code into it.
_Avoid_: Tier, module

**Generated code**:
Source emitted by the Generator into a Layer's `src/generated/` directory. Never hand-edited; deleted by `clean` and recreated by `maiaGeneration`.
_Avoid_: Boilerplate

**Custom code**:
Hand-written source that lives beside Generated code in a Layer.
_Avoid_: Manual code

**Showcase**:
The reference application in `maia-showcase` that exercises every Generator feature and is the target of the Playwright integration tests.
_Avoid_: Demo, example app

### Entities and types

**Entity**:
A persisted domain object declared in a Spec with `entity()`, mapping to one database table. Many-to-many entities are kinds of Entity.
_Avoid_: Model, record, table

**Versioned entity**:
An Entity declared `versioned = true`, carrying a version number used for optimistic locking.
_Avoid_: Locked entity

**Version history**:
The feature, enabled with `recordVersionHistory = true`, that makes the Generator derive a History entity recording every version of an Entity. Implies a Versioned entity.
_Avoid_: Audit trail

**History entity**:
The Entity the Generator derives, named `<Entity>History`, for an Entity declared with version history. Holds one row per version of the audited record.
_Avoid_: Audit table

**Entity inheritance**:
An Entity declared with `superclass(parent)` that inherits the parent's fields.
_Avoid_: Subclass table, polymorphic entity

**Type discriminator**:
The value, set with `typeDiscriminator`, that identifies which subtype an inherited Entity's row belongs to.
_Avoid_: Type code

**Abstract entity**:
An Entity declared `isAbstract = true` that only serves as a parent in Entity inheritance and is never instantiated on its own.
_Avoid_: Base table

**Effective range**:
A pair of optional effective-from and effective-to bounds (timestamps or dates) on an Entity, managed either by the user or by the system, so that a record is only in force between them.
_Avoid_: Validity period, active dates

**Microtype**:
A class wrapping a primitive to give a field its own domain type, declared in a Spec with `stringType`, `intType`, `longType` or `booleanType`.
_Avoid_: Type, wrapper type

**Value class**:
The Kotlin inline-class variant of a Microtype, declared in a Spec with `stringValueClass` or `booleanValueClass`.
_Avoid_: Wrapper

**Provided type**:
A Microtype or Value class whose class is hand-written and only referenced by the Spec, marked with `provided()`.
_Avoid_: External type

### User interface

**Searchable DTO**:
A read-model projection of one or more Entities, declared with `searchableDto()`, with a generated search endpoint.
_Avoid_: View, query model

**Blotter**:
The AG Grid table that displays a Searchable DTO.
_Avoid_: Grid, table, list view

**Paginated blotter**:
A Blotter that shows its results one page at a time, with controls to move between pages and choose the page size. The default mode.
_Avoid_: Paged grid

**Infinite-scroll blotter**:
A Blotter that loads more results as the user scrolls down. Declared with `infiniteScroll()`.
_Avoid_: Endless list

**Filter model**:
The per-column filter conditions a Blotter sends with a search. Several conditions on one column are combined with AND or OR; conditions on different columns are always ANDed.
_Avoid_: Filter state, search criteria

**Blank**:
A value that is null or missing, or, for text, the empty string. Filters for blank values match it, and negative filters such as "not equal" exclude it.
_Avoid_: Empty, null value

**Blotter page**:
The Angular page that hosts a Blotter, declared with `blotterPage()` or `withBlotterPage {}`.
_Avoid_: List page

**Short ID**:
The truncated display of a uuid, with the full value available in a tooltip on hover and a copy-to-clipboard control. Used wherever a uuid is shown to the user, in forms and in Blotter cells.
_Avoid_: Truncated uuid

### Runtime services

**Authority**:
A function that can be performed on some aspect of the domain model, usually one of the CRUD operations. Declared statically in a Spec with `authority()`, never created dynamically.
_Avoid_: Permission, privilege

**CrudListener**:
A per-Entity callback interface that is told when an Entity instance is created, updated or deleted. Created, updated and deleted callbacks run after the transaction commits, outside it; the before-update and before-delete callbacks run inside the transaction, before the change.
_Avoid_: Observer, hook

**CrudNotifier**:
The per-Entity component that delivers CRUD events to every CrudListener. It only notifies listeners of changes that were committed.
_Avoid_: Publisher, dispatcher

**Props**:
The lookup facade for named application properties. Returns a Property override when one exists, otherwise the Spring Environment value.
_Avoid_: Config, settings

**Property override**:
A runtime-editable value for a named property, stored in the Props repo and taking precedence over the Spring Environment value of the same name.
_Avoid_: Dynamic property

**Feature**:
A class extending `Feature` that names a runtime-switchable behaviour. Its name is the class name.
_Avoid_: Flag

**Feature toggle**:
The persisted on/off state of a Feature, which can be flipped at runtime without redeploying.
_Avoid_: Feature flag

**Activation strategy**:
A pluggable rule that decides, from stored parameters, whether a toggled Feature is active for a given request.
_Avoid_: Toggle condition

**Job**:
A Spring bean implementing `MaiaJob`, identified by a unique job name and run on demand by the job service.
_Avoid_: Task, batch

**Job execution**:
One recorded run of a Job, with a status of running, success, failed or abandoned.
_Avoid_: Job run

### Elasticsearch

**Index**:
One versioned Elasticsearch index managed by Maia, identified by a base name and a version. Only one version of a base name is active at a time.
_Avoid_: Collection, table

**Document count**:
The number of documents in one Index, shown as "Docs" on the Elastic Indices dashboard.
_Avoid_: Record count, row count
