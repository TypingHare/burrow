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
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.1")
    implementation("org.springframework.boot:spring-boot-loader:3.3.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.1")

    // Picocli and JLine
    implementation("info.picocli:picocli:4.7.6")
    implementation("org.jline:jline:3.26.1")

    // Apache Http Client
    implementation("org.apache.httpcomponents.client5:httpclient5-cache:5.3.1")

    // Utility
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.guava:guava:33.2.0-jre")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}