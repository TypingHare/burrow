plugins { kotlin("jvm") version "2.0.20" }
repositories { mavenCentral() }
kotlin { jvmToolchain(21) }

group = "burrow"
version = "0.0.0"

dependencies {
    implementation(files("{{home_dir}}/.burrow/libs/burrow.jar"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("{{carton_name}}.carton")
    archiveVersion.set("")
}
