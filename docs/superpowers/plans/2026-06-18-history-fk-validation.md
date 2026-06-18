# History FK Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Throw `ModelDefinitionException` when an entity with a history table has a FK to an entity without a history table.

**Architecture:** Add a cross-entity validation pass inside `AbstractSpec.finalizeEntityDefs()` that iterates over all entities with `withVersionHistory = true`, inspects their FK fields, and throws if any referenced entity lacks version history. Fix the showcase spec to satisfy the new constraint by adding `recordVersionHistory = true` to `leftManyEntityDef`.

**Tech Stack:** Kotlin, JUnit Jupiter, AssertJ

**User Verification:** NO

---

### Task 1: Add validation + tests

**Goal:** Guard in `finalizeEntityDefs()` throws `ModelDefinitionException` when a history entity FKs a non-history entity, with tests proving both the violation and the happy path.

**Files:**
- Modify: `maia-gen/maia-gen-spec/build.gradle.kts`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/AbstractSpec.kt:193-205`
- Create: `maia-gen/maia-gen-spec/src/test/kotlin/org/maiaframework/gen/spec/HistoryFkValidationTest.kt`

**Acceptance Criteria:**
- [ ] `ModelDefinitionException` thrown when history entity FKs a non-history entity (regular entity case)
- [ ] `ModelDefinitionException` thrown when many-to-many join entity with history FKs a non-history entity
- [ ] No exception when history entity only FKs other history entities
- [ ] `./gradlew :maia-gen:maia-gen-spec:test` passes

**Verify:** `./gradlew :maia-gen:maia-gen-spec:test` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Add test dependencies to `maia-gen/maia-gen-spec/build.gradle.kts`**

```kotlin
dependencies {

    api(project(":libs:maia-jdbc"))
    api(project(":libs:maia-lang"))
    api(project(":libs:maia-domain"))

    testImplementation(platform(project(":maia-platform")))
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}
```

- [ ] **Step 2: Write the failing tests**

Create `maia-gen/maia-gen-spec/src/test/kotlin/org/maiaframework/gen/spec/HistoryFkValidationTest.kt`:

```kotlin
package org.maiaframework.gen.spec

import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDefinitionException
import org.maiaframework.gen.spec.definition.ReferencedEntity
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes

class HistoryFkValidationTest {

    @Test
    fun `regular entity with history table FKing a non-history entity throws ModelDefinitionException`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val noHistory = entity("com.example", "NoHistory") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val withHistory = entity("com.example", "WithHistory", recordVersionHistory = true) {
                foreignKey("noHistory", noHistory) { fieldDisplayName("No History") }
            }

        }

        assertThatThrownBy { spec.modelDef }
            .isInstanceOf(ModelDefinitionException::class.java)
            .hasMessageContaining("WithHistory")
            .hasMessageContaining("NoHistory")

    }


    @Test
    fun `many-to-many join entity with history FKing a non-history entity throws ModelDefinitionException`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val noHistory = entity("com.example", "NoHistory", nameFieldForPkAndNameDto = "name") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val withHistory = entity("com.example", "WithHistory", recordVersionHistory = true, nameFieldForPkAndNameDto = "name") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val join = manyToManyEntity(
                "com.example",
                "Join",
                recordVersionHistory = true,
                leftEntity = ReferencedEntity("left", "Left", noHistory, IsEditableByUser.TRUE),
                rightEntity = ReferencedEntity("right", "Right", withHistory, IsEditableByUser.TRUE)
            )

        }

        assertThatThrownBy { spec.modelDef }
            .isInstanceOf(ModelDefinitionException::class.java)
            .hasMessageContaining("Join")
            .hasMessageContaining("NoHistory")

    }


    @Test
    fun `history entity FKing another history entity is valid`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val parent = entity("com.example", "Parent", recordVersionHistory = true) {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val child = entity("com.example", "Child", recordVersionHistory = true) {
                foreignKey("parent", parent) { fieldDisplayName("Parent") }
            }

        }

        assertThatNoException().isThrownBy { spec.modelDef }

    }

}
```

- [ ] **Step 3: Run tests — expect 3 failures (validation not yet implemented)**

```bash
./gradlew :maia-gen:maia-gen-spec:test
```

Expected: first two tests FAIL (no exception thrown), third test PASSES.

- [ ] **Step 4: Add validation to `AbstractSpec.finalizeEntityDefs()`**

In `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/AbstractSpec.kt`, update `finalizeEntityDefs()` (currently lines 193–205):

```kotlin
private fun finalizeEntityDefs() {

    if (entityDefsFinalized) return

    entityDefs.forEach { entityDef ->
        entityDef.initManyToManyAssociations(
            manyToManyAssociationsByEntityName[entityDef.entityBaseName] ?: emptyList()
        )
    }

    entityDefs
        .filter { it.withVersionHistory.value }
        .forEach { entityDef ->
            entityDef.allForeignKeyEntityFieldDefs
                .mapNotNull { it.foreignKeyFieldDef?.foreignEntityDef }
                .filter { !it.withVersionHistory.value }
                .forEach { foreignEntityDef ->
                    throw ModelDefinitionException(
                        "Entity '${entityDef.entityBaseName}' has version history " +
                        "but references '${foreignEntityDef.entityBaseName}' which does not. " +
                        "Entities with history tables may only reference entities that also have history tables."
                    )
                }
        }

    entityDefsFinalized = true

}
```

- [ ] **Step 5: Run tests — expect all 3 to pass**

```bash
./gradlew :maia-gen:maia-gen-spec:test
```

Expected: `BUILD SUCCESSFUL`, all 3 tests pass.

- [ ] **Step 6: Commit**

```bash
git add maia-gen/maia-gen-spec/build.gradle.kts \
        maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/AbstractSpec.kt \
        maia-gen/maia-gen-spec/src/test/kotlin/org/maiaframework/gen/spec/HistoryFkValidationTest.kt
git commit -m "$(cat <<'EOF'
feat: throw ModelDefinitionException when history entity FKs a non-history entity

Entities with history tables may only reference entities that also have
history tables, or historical FK references become dangling after deletion.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"
```

```json:metadata
{"files": ["maia-gen/maia-gen-spec/build.gradle.kts", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/AbstractSpec.kt", "maia-gen/maia-gen-spec/src/test/kotlin/org/maiaframework/gen/spec/HistoryFkValidationTest.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-spec:test", "acceptanceCriteria": ["ModelDefinitionException thrown for regular history->non-history FK", "ModelDefinitionException thrown for many-to-many history->non-history FK", "No exception for history->history FK", "tests pass"], "requiresUserVerification": false}
```

---

### Task 2: Fix showcase spec + update SQL migration

**Goal:** `leftManyEntityDef` gets `recordVersionHistory = true`, generated SQL is regenerated, and the migration is updated to add the `version` column to `left_many` and the `left_many_history` table.

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt:1260`
- Modify (regenerate): `maia-showcase/dao/src/generated/sql/create_entity_tables.sql`
- Modify: `maia-showcase/dao/src/main/resources/db/migration/V003__create_entity_tables.sql`

**Acceptance Criteria:**
- [ ] `./gradlew :maia-showcase:dao:maiaGeneration` succeeds (no `ModelDefinitionException`)
- [ ] `left_many` has `version bigint NOT NULL` in both generated SQL and V003 migration
- [ ] `left_many_history` table exists in both generated SQL and V003 migration
- [ ] `./gradlew :maia-gen:maia-gen-spec:test` still passes

**Verify:** `./gradlew :maia-showcase:dao:maiaGeneration` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Add `recordVersionHistory = true` to `leftManyEntityDef` in `MaiaShowcaseSpec.kt`**

At line 1260 of `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt`, change:

```kotlin
    val leftManyEntityDef = entity(
        "org.maiaframework.showcase.many_to_many",
        "LeftMany",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        nameFieldForPkAndNameDto = "someString",
    ) {
```

to:

```kotlin
    val leftManyEntityDef = entity(
        "org.maiaframework.showcase.many_to_many",
        "LeftMany",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        nameFieldForPkAndNameDto = "someString",
        recordVersionHistory = true,
    ) {
```

- [ ] **Step 2: Regenerate showcase DAO SQL**

```bash
./gradlew :maia-showcase:dao:maiaGeneration
```

Expected: `BUILD SUCCESSFUL`. This regenerates `maia-showcase/dao/src/generated/sql/create_entity_tables.sql`.

- [ ] **Step 3: Inspect the new `left_many` DDL in the generated file**

Check that `maia-showcase/dao/src/generated/sql/create_entity_tables.sql` now contains:
- `left_many` with `version bigint NOT NULL`
- A new `left_many_history` table (structure matches `right_many_history`)

Expected shape (confirm actual output matches this):
```sql
CREATE TABLE maia.left_many (
    created_timestamp_utc timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    version bigint NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE maia.left_many_history (
    change_type text NOT NULL,
    created_timestamp_utc timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    version bigint NOT NULL,
    PRIMARY KEY(id, version)
);
```

- [ ] **Step 4: Update `V003__create_entity_tables.sql` migration**

In `maia-showcase/dao/src/main/resources/db/migration/V003__create_entity_tables.sql`, find the current `left_many` DDL (around line 413) and replace it with the new one from the generated file (which now includes `version bigint NOT NULL`). Then insert the `left_many_history` table DDL immediately after `left_many`.

The existing block to replace:
```sql
CREATE TABLE maia.left_many (
    created_timestamp_utc timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);
```

Replace with (adding `version bigint NOT NULL` and the history table):
```sql
CREATE TABLE maia.left_many (
    created_timestamp_utc timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    version bigint NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE maia.left_many_history (
    change_type text NOT NULL,
    created_timestamp_utc timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    version bigint NOT NULL,
    PRIMARY KEY(id, version)
);
```

> **Note:** The exact column order comes from the regenerated file. Use the actual output from Step 3, not the template above, if they differ.

- [ ] **Step 5: Recreate local DB to apply the updated migration**

```bash
docker compose -f maia-showcase/compose.yaml down && docker compose -f maia-showcase/compose.yaml up -d
```

- [ ] **Step 6: Commit**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt \
        maia-showcase/dao/src/generated/sql/create_entity_tables.sql \
        maia-showcase/dao/src/main/resources/db/migration/V003__create_entity_tables.sql
git commit -m "$(cat <<'EOF'
fix(showcase): add recordVersionHistory to LeftMany so join history constraint passes

LeftToRightSimpleJoin has version history and FKs both LeftMany and
RightMany — both must also have history tables to satisfy the new
generator validation.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"
```

```json:metadata
{"files": ["maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt", "maia-showcase/dao/src/generated/sql/create_entity_tables.sql", "maia-showcase/dao/src/main/resources/db/migration/V003__create_entity_tables.sql"], "verifyCommand": "./gradlew :maia-showcase:dao:maiaGeneration", "acceptanceCriteria": ["maiaGeneration succeeds", "left_many has version column in migration", "left_many_history table in migration"], "requiresUserVerification": false}
```
