import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "burrow"
version = "0.0.0"

plugins {
    kotlin("jvm") version "2.0.20"
    @Suppress("SpellCheckingInspection")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

tasks {
    withType<ShadowJar> {
        archiveBaseName.set("burrow")
        archiveVersion.set(version.toString())

        manifest {
            attributes["Main-Class"] = "burrow.client.BurrowClient"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Log
    implementation("ch.qos.logback:logback-classic:1.5.15")

    // Terminal
    implementation("info.picocli:picocli:4.7.6")
    implementation("org.jline:jline:3.27.1")

    // Tools
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.reflections:reflections:0.10.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}