plugins {
    id("java")
}

group = "com.nowhere.plugin"
version = "0.0.7"

repositories {
    mavenCentral()

    // CurseMaven for MultipleHUD
    maven {
        name = "cursemaven"
        url = uri("https://cursemaven.com")
    }

    // Alternative: Hytale Mods Maven (if available)
    maven {
        name = "hytale-mods"
        url = uri("https://maven.hytale-mods.dev/")
    }
}

dependencies {
    // Hytale Server API (local JAR)
    compileOnly(files("libs/HytaleServer.jar"))

    // MultipleHUD - Optional dependency for multi-HUD compatibility
    // Project ID: 1423634, File ID: 7530266 (version 1.0.4)
    // This is optional - the mod will work without it but won't be compatible with other HUD mods
    compileOnly("curse.maven:multiplehud-1423634:7530266")

    // Test dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory.set(file("C:\\Users\\Cohes\\Desktop\\Server\\Server\\mods"))

    from("src/main/resources")

    // Add manifest attributes
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:unchecked")
    options.encoding = "UTF-8"
}

// Custom task to display build information
tasks.register("buildInfo") {
    doLast {
        println("=========================================")
        println("  SoulDisplay Build Information")
        println("=========================================")
        println("Name: ${project.name}")
        println("Version: ${project.version}")
        println("Group: ${project.group}")
        println("Output: ${tasks.jar.get().destinationDirectory.get()}")
        println("=========================================")
    }
}

// Run buildInfo after successful build
tasks.build {
    finalizedBy("buildInfo")
}