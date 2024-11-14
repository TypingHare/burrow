import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

tasks {
    withType<ShadowJar> {
        archiveBaseName.set("burrow")
        archiveVersion.set("0.0.0")

        manifest {
            attributes["Main-Class"] = "burrow.server.BurrowServer"
        }
    }
}

group = "burrow"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // SFML
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // Terminal
    implementation("info.picocli:picocli:4.7.6")
    implementation("org.jline:jline:3.27.1")
    implementation("commons-cli:commons-cli:1.9.0")

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