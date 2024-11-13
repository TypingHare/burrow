plugins {
    kotlin("jvm") version "2.0.20"
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