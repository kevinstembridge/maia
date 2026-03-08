import java.net.URLClassLoader
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-library-spring-conventions")
    idea
}


val maiagen by configurations.creating
val flyway by configurations.creating


dependencies {

    implementation(project(":maia-showcase:domain"))

    api("org.maiaframework:maia-common")
//    api("org.maiaframework:maia-csv")
    api("org.maiaframework:maia-domain")
    api("org.maiaframework:maia-lang")
//    api("org.maiaframework:maia-staging-csv")
//    api("org.maiaframework:maia-webapp-domain")

//    implementation("org.maiaframework:maia-hazelcast")
    implementation("org.maiaframework:maia-jdbc")

//    implementation("com.hazelcast:hazelcast")
//    implementation("org.hibernate.validator:hibernate-validator")

//    testImplementation("org.junit.jupiter:junit-jupiter")
//    testImplementation("org.assertj:assertj-core")

//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    maiagen(project(":maia-gen:maia-gen-generator"))
    maiagen(project(":maia-showcase:spec"))

    flyway("org.flywaydb:flyway-core:11.14.1")
    flyway("org.flywaydb:flyway-database-postgresql:11.14.1")
    flyway("org.postgresql:postgresql:42.7.9")

}



sourceSets {
    main {
        java.srcDir("src/generated/kotlin/main")
        resources.srcDir("src/generated/resources/main")
    }
    test {
        java.srcDir("src/generated/kotlin/test")
    }
}


tasks {
    clean {
        delete("src/generated")
    }
}


tasks.register<JavaExec>("maiaGeneration") {

    inputs.files(file("../spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/kotlin/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.DaoLayerModuleGeneratorKt")
    args("specificationClassNames=org.maiaframework.showcase.MaiaShowcaseSpec,org.maiaframework.showcase.MaiaShowcasePartySpec")

}



tasks.register("flywayRepair") {

    description = "Runs Flyway repair against the local database"
    group = "flyway"

    val flywayFiles = configurations["flyway"].files
    val migrationDirs = sourceSets.main.get().resources.srcDirs

    doLast {
        val urls = flywayFiles.map { it.toURI().toURL() } +
                migrationDirs.filter { it.exists() }.map { it.toURI().toURL() }

        val classLoader = URLClassLoader(urls.toTypedArray(), Thread.currentThread().contextClassLoader)

        val flywayClass = classLoader.loadClass("org.flywaydb.core.Flyway")
        val configureMethod = flywayClass.getMethod("configure", ClassLoader::class.java)
        val fluentConfig = configureMethod.invoke(null, classLoader)

        val fluentClass = fluentConfig.javaClass
        fluentClass.getMethod("dataSource", String::class.java, String::class.java, String::class.java)
            .invoke(fluentConfig, "jdbc:postgresql://localhost:5433/maia_db", "maia_owner", "maia_owner_password")
        fluentClass.getMethod("defaultSchema", String::class.java)
            .invoke(fluentConfig, "maia")
        fluentClass.getMethod("locations", Array<String>::class.java)
            .invoke(fluentConfig, arrayOf("classpath:db/migration"))

        val flyway = fluentClass.getMethod("load").invoke(fluentConfig)
        flyway.javaClass.getMethod("repair").invoke(flyway)

        logger.lifecycle("Flyway repair completed successfully")
    }

}


tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}
