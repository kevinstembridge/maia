# Maia

Maia is a framework for building full-stack web applications or microservices on a Kotlin, Angular, Spring Boot and PostgreSQL tech stack.

At its heart is a code generator. You write a **Spec** in a Kotlin DSL, and the Generator produces the code for every Layer of the application: domain, DAO, repository, service, web endpoints and the Angular UI. The generated source is committed, so you can read it, debug it, and see in a diff what a Spec change does.

See [docs/README.md](docs/README.md) for the motivation.

## Where things are

| Directory | What it holds |
| --- | --- |
| `maia-gen/` | The Generator: the Spec DSL (`maia-gen-spec`), the generator itself, and the schema check |
| `maia-showcase/` | Reference application that exercises every Generator feature, with Playwright tests |
| `libs/` | Reusable framework modules (jobs, props, toggles, JWT, CSV, the Angular UI workspace, etc.) |
| `maia-platform/` | Bill of materials for all dependency versions |
| `plugins/` | Gradle plugin and testing support |
| `buildSrc/` | Gradle convention plugins (`maia.kotlin-*-conventions`) |

## Building

Requires JDK 21 and Node 24.

```
./gradlew clean build -Dplaywright.headless=true
```

Regenerate the code for one module with `./gradlew :<module>:maiaGeneration`. Never edit files under `src/generated/` by hand.

To run the Showcase locally, start PostgreSQL and pgAdmin with `docker compose -f maia-showcase/compose.yaml up -d`.

## Further reading

- [GLOSSARY.md](GLOSSARY.md): the project's domain language
- [docs/adr/](docs/adr): architecture decisions and the reasons behind them
