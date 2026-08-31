# maia-gen-schema-check

Compares the actual schema of a PostgreSQL database against the schema *implied* by an
application's `ApplicationModelDef` (i.e. what the generator would derive from an
`AbstractSpec` and its entity hierarchies), and reports any drift — missing tables/columns,
type or nullability mismatches, missing primary keys, missing foreign keys, missing indexes.

It's a drift detector, not a migration tool: it never modifies the database. Use it to catch
cases where Flyway migrations have fallen out of sync with the spec that generated them.

## How it works

1. Instantiates the given `AbstractSpec` class (via `ApplicationModelDefInstantiator`) and
   extracts the *expected* schema from its `rootEntityHierarchies` (`ExpectedSchemaExtractor`).
2. Connects to the target database and introspects the *actual* schema for every schema
   referenced by the expected tables (`PostgresSchemaIntrospector`).
3. Diffs expected vs. actual (`SchemaComparator`) and renders a report, either as
   human-readable text or JSON (`SchemaCheckReporter`).

## Usage

There are two ways to run it: as the `maiaSchemaCheck` Gradle task (recommended, wired up by
`maia-gradle-plugin`), or standalone from the command line.

### Gradle task

Any module that applies the `org.maiaframework.maia-generation` plugin automatically gets a
`maiaSchemaCheck` task. Configure it via the `maia { }` extension:

```kotlin
plugins {
    id("org.maiaframework.maia-generation")
}

maia {
    schemaCheckApplicationSpecClassName.set("org.maiaframework.showcase.MaiaShowcaseApplicationSpec")
    schemaCheckJdbcUrl.set("jdbc:postgresql://localhost:5433/maia_showcase")
    schemaCheckUsername.set("maia")
    // schemaCheckPassword.set("...")   // or leave unset and use MAIA_SCHEMA_CHECK_DB_PASSWORD
    // schemaCheckOutputFormat.set("json")
    // schemaCheckOutputFile.set(layout.buildDirectory.file("reports/schema-check.txt"))
    // schemaCheckIgnoreErrors.set(true)
}
```

Then run:

```shell
MAIA_SCHEMA_CHECK_DB_PASSWORD=secret ./gradlew :your-module:maiaSchemaCheck
```

By default the task **fails the build** if any drift is found. Set `schemaCheckIgnoreErrors`
to `true` to have it report drift without failing (it still fails on connection/setup errors
regardless of this setting).

| Extension property                     | Required | Default | Notes                                                      |
|-----------------------------------------|----------|---------|--------------------------------------------------------------|
| `schemaCheckApplicationSpecClassName`   | yes      | —       | Fully-qualified `AbstractSpec` subclass name                |
| `schemaCheckJdbcUrl`                    | yes      | —       | e.g. `jdbc:postgresql://localhost:5433/maia_showcase`       |
| `schemaCheckUsername`                   | yes      | —       |                                                                |
| `schemaCheckPassword`                   | no       | —       | Falls back to `MAIA_SCHEMA_CHECK_DB_PASSWORD` env var        |
| `schemaCheckOutputFormat`               | no       | `text`  | `text` or `json`                                             |
| `schemaCheckOutputFile`                 | no       | —       | Writes report to a file instead of stdout                    |
| `schemaCheckIgnoreErrors`               | no       | `false` | If `true`, drift is reported but doesn't fail the task        |

### Standalone CLI

The module also builds a runnable `main` (`SchemaCheckMain`), taking `key=value` arguments:

```shell
MAIA_SCHEMA_CHECK_DB_PASSWORD=secret java -cp <classpath> \
  org.maiaframework.gen.schemacheck.SchemaCheckMainKt \
  applicationSpecClassName=org.maiaframework.showcase.MaiaShowcaseApplicationSpec \
  jdbcUrl=jdbc:postgresql://localhost:5433/maia_showcase \
  username=maia \
  format=json \
  outputFile=schema-check-report.json
```

| Argument                  | Required | Default | Notes                                                  |
|----------------------------|----------|---------|----------------------------------------------------------|
| `applicationSpecClassName` | yes      | —       | Fully-qualified `AbstractSpec` subclass name             |
| `jdbcUrl`                  | yes      | —       |                                                            |
| `username`                 | yes      | —       |                                                            |
| `password`                 | no       | —       | Falls back to `MAIA_SCHEMA_CHECK_DB_PASSWORD` env var     |
| `format`                   | no       | `text`  | `text` or `json`                                          |
| `outputFile`                | no       | —       | Writes report to a file instead of stdout                |

Exit codes:

- `0` — schema matches, no drift found
- `1` — drift found (report printed/written)
- `2` — the check itself failed to run (bad spec class, can't connect, etc.)

### Sample text output

```
✗ maia.party [MISMATCH]
    ERROR: missing column 'archived_at' (timestamptz, nullable=true)
    ERROR: column 'authorities' expected varchar/nullable=false, actual varchar/nullable=true
✓ maia.address [MATCH]
```
