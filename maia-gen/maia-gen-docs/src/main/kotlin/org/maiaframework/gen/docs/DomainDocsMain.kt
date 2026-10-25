package org.maiaframework.gen.docs

import org.maiaframework.gen.spec.ApplicationSpec
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess


/**
 * Usage: `DomainDocsMain <outputDir> <SpecClassName>...`
 */
fun main(args: Array<String>) {

    if (args.size < 2) {
        System.err.println("Usage: DomainDocsMain <outputDir> <SpecClassName>...")
        exitProcess(2)
    }

    try {
        runDomainDocs(args.drop(1), Paths.get(args[0]))
    } catch (t: Throwable) {
        System.err.println("Domain docs failed: ${t.message}")
        exitProcess(2)
    }

}


/**
 * Instantiates the named [ApplicationSpec] classes and writes the Domain docs site to [outputDir].
 */
fun runDomainDocs(specClassNames: List<String>, outputDir: Path) {

    val specs = specClassNames.map { `instantiate spec`(it) }

    generateDomainDocs(specs, outputDir)

}


private fun `instantiate spec`(className: String): ApplicationSpec {

    val clazz = try {
        Class.forName(className, true, Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader())
    } catch (e: ClassNotFoundException) {
        throw IllegalArgumentException("Spec class '$className' could not be loaded: not found on the classpath", e)
    }

    val instance = try {
        clazz.getDeclaredConstructor().newInstance()
    } catch (e: ReflectiveOperationException) {
        throw IllegalArgumentException("Spec class '$className' could not be instantiated: ${e.cause?.message ?: e.message}", e)
    }

    return instance as? ApplicationSpec
        ?: throw IllegalArgumentException("Spec class '$className' is not an ApplicationSpec")

}
