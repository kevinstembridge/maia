import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}


repositories {
    mavenCentral()
}


kotlin {
    compilerOptions {
        suppressWarnings.set(true)
        jvmTarget.set(JvmTarget.JVM_21)
    }
}


tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}


tasks.withType<Test>{
    useJUnitPlatform()
}

