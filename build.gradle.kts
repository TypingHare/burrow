plugins {
    id("java")
}

group = "me.jameschan"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.5")

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