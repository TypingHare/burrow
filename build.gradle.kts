group = "burrow"
version = "0.0.0"

val mainClass = "burrow.Main"
val imageName = "burrow"

plugins {
    // Enables Kotlin compilation for JVM projects
    kotlin("jvm") version "2.0.21"

    // Simplifies the configuration and packaging of JVM-based applications
    application

    // Creates a "fat jar" or "uber jar" that packages the application and all
    // the dependencies into a single JAR file
    id("com.github.johnrengelman.shadow") version "8.1.1"

    // Integrates GraalVM Native Image generation into the build process
    id("org.graalvm.buildtools.native") version "0.10.3"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Terminal
    implementation("info.picocli:picocli:4.7.6")
    implementation("org.jline:jline:3.27.1")

    // JSON support
    implementation("com.google.code.gson:gson:2.11.0")

    // Package scanner
    implementation("org.reflections:reflections:0.10.2")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set(mainClass)
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set(imageName)
            mainClass.set(mainClass)
        }
    }
}