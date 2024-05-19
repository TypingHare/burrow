plugins {
    id("java")
    id("org.springframework.boot") version "3.2.5"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.jameschan"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.5")
    implementation("org.springframework.boot:spring-boot-loader:3.2.5")

    // Picocli
    implementation("info.picocli:picocli:4.7.6")

    // Apache
    implementation("org.apache.httpcomponents.client5:httpclient5-cache:5.3.1")

    // Utility
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.guava:guava:33.2.0-jre")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("server") {
    group = "application"
    description = "Alias for bootRun"
    dependsOn("bootRun")
}

tasks.register<JavaExec>("cli") {
    group = "application"
    description = "Run the Burrow CLI."
    mainClass.set("me.jameschan.burrow.BurrowCli")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "org.springframework.boot.loader.launch.JarLauncher"
        attributes["Start-Class"] = "me.jameschan.burrow.BurrowServer"
    }
}