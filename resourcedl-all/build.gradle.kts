import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}
apply (plugin = "com.github.johnrengelman.shadow")

version = "0.1.0-SNAPSHOT"

val env: Map<String, String?> = System.getenv()
val properties: java.util.Properties = System.getProperties()

val excludeConsoleApp : Boolean get()
        = env.containsKey("EXCLUDE_RD_CONSOLE_APP") ||
            "true" == properties.getProperty("resourcedl.build.excludeConsoleApp")

dependencies {
    shadow(project(":"))
    if (!excludeConsoleApp) {
        shadow(project(":resourcedl-consoleapp"))
    }
}

shadow

tasks.getByName("shadowJar", ShadowJar::class) {
    manifest { attributes["Main-Class"] = "xland.ioutils.resourcedl.Main" }
    minimize {
        exclude (project(":"))
        exclude (project(":resourcedl-consoleapp"))
    }
    relocate("org.slf4j", "xland.ioutils.resourcedl.slf4j")
}
