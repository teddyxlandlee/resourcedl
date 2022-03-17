import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.objectweb.asm.ClassWriter
import java.nio.file.Files
import java.nio.file.Paths
import org.objectweb.asm.Opcodes as ops

buildscript {
    dependencies {
        classpath(group = "org.ow2.asm", name = "asm", version = "9.2")
    }
}

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}
apply (plugin = "com.github.johnrengelman.shadow")

fun String.toSlashed() = this.replace('.', '/')

class ModuleInfo(val moduleName: String, val moduleVersion: String? = null, val open: Boolean = false) {
    private val exports: MutableMap<String, Array<out String>?> = mutableMapOf()
    private val requires: MutableMap<String, String?> = mutableMapOf()
    private val requiresTransitive: MutableMap<String, String?> = mutableMapOf()
    private val opens: MutableMap<String, Array<out String>?> = mutableMapOf()
    private val provides: MutableMap<String, Array<out String>?> = mutableMapOf()

    private val requiresList = mutableSetOf<String>()

    fun toByteArray() : ByteArray {
        val cw = ClassWriter(0)
        cw.visit(ops.V9, ops.ACC_MODULE, "module-info",
                null, null, null)
        cw.visitSource("asm-gen:teddyxlandlee", null)
        cw.visitModule(moduleName,
                if (open) ops.ACC_OPEN else 0,
                moduleVersion).run {
            requires.forEach { (module, version) ->
                visitRequire(module, 0, version)
            }
            requiresTransitive.forEach { (module, version) ->
                visitRequire(module, ops.ACC_TRANSITIVE, version)
            }
            if ("java.base" !in requiresList)
                visitRequire("java.base", ops.ACC_MANDATED, null)

            exports.forEach { (pkg, to) ->
                visitExport(pkg, 0, *(to ?: arrayOf()))
            }
            opens.forEach { (pkg, to) ->
                visitOpen(pkg, 0, *(to ?: arrayOf()))
            }
            provides.forEach { (spi, impl) ->
                visitProvide(spi, *(impl ?: arrayOf()))
            }
        }
        cw.visitEnd()
        return cw.toByteArray()
    }

    fun exports(pkg: String, vararg to: String) {
        exports[pkg.toSlashed()] = to
    }

    fun exports(pkg: String) {
        exports[pkg.toSlashed()] = null
    }

    private fun checkReqExists(module: String) {
        if (!requiresList.add(module)) {    // already exists
            requires.remove(module)
            requiresTransitive.remove(module)
        }
    }

    fun requires(module: String, version: String?) {
        checkReqExists(module)
        requires[module] = version
    }

    fun requires(vararg modules: String) {
        modules.forEach(::requires)
    }

    fun requires(module: String) {
        checkReqExists(module)
        requires[module] = null
    }

    fun requiresTransitive(module: String, version: String?) {
        checkReqExists(module)
        requiresTransitive[module] = version
    }

    fun requiresTransitive(vararg modules: String) {
        modules.forEach(::requiresTransitive)
    }

    fun requiresTransitive(module: String) {
        checkReqExists(module)
        requiresTransitive[module] = null
    }

    fun opens(pkg: String, vararg to: String) {
        if (open)
            throw kotlin.RuntimeException("module is open")
        opens[pkg.toSlashed()] = to
    }

    fun opens(pkg: String) {
        if (open)
            throw RuntimeException("module is open")
        opens[pkg.toSlashed()] = null
    }

    fun opens(vararg packages: String) {
        packages.forEach(::opens)
    }

    fun provides(spi: String, vararg impl: String) {
        if (impl.isEmpty()) throw kotlin.RuntimeException("Implementation of $spi should not be empty")
        val impl0 = Array(impl.size) { impl[it].toSlashed() }
        provides[spi.toSlashed()] = impl0
    }
}

val theModuleInfo = ModuleInfo(
        "xland.ioutils.resourcedl.all",
        open = true
).apply {
    exports("xland.ioutils.resourcedl.download")
    exports("xland.ioutils.resourcedl.hashing")
    exports("xland.ioutils.resourcedl.util")
    exports("xland.ioutils.resourcedl.util.spi")
    exports("xland.ioutils.resourcedl")

    exports("xland.ioutils.resourcedl.console")
    provides("xland.ioutils.resourcedl.util.spi.ConsoleRDAppProvider",
            "xland.ioutils.resourcedl.console.main.ConsoleApp")

    exports("xland.ioutils.resourcedl.multifile.json")
    provides("xland.ioutils.resourcedl.util.spi.MultiFileDownloadProvider",
            "xland.ioutils.resourcedl.multifile.json.JsonMultiFileDownloadProvider")
}

version = rootProject.property("version").toString()

val env: Map<String, String?> = System.getenv()
val properties: java.util.Properties = System.getProperties()

val excludeConsoleApp : Boolean get()
        = env.containsKey("EXCLUDE_RD_CONSOLE_APP") ||
            "true" == properties.getProperty("resourcedl.build.excludeConsoleApp")
val excludeJsonMultiFile : Boolean get()
        = env.containsKey("EXCLUDE_RD_JSON_MULTIFILE") ||
            "true" == properties.getProperty("resourcedl.build.excludeJsonMultiFile")

dependencies {
    shadow(project(":"))
    if (!excludeConsoleApp) {
        shadow(project(":resourcedl-consoleapp"))
    }
    if (!excludeJsonMultiFile) {
        shadow(project(":resourcedl-json-multifile"))
    }
}

fun createFile(dest : java.nio.file.Path) : File {
    val bytes = theModuleInfo.toByteArray()
    //buildDir.toPath()
    dest.resolve(Paths.get("META-INF", "versions", "9", "module-info.class"))
            .run {
                Files.createDirectories(this.parent)
                Files.newOutputStream(this).run {
                    this.write(bytes)
                    this.close()
                }
            }
    return dest.toFile().resolve("META-INF").apply { this.deleteOnExit() }
}

tasks.processResources {
    doLast {
        val dest = this@processResources.destinationDir.toPath()
        val bytes = theModuleInfo.toByteArray()
        dest.resolve(Paths.get("META-INF", "versions", "9", "module-info.class"))
                .run {
                    Files.createDirectories(this.parent)
                    Files.newOutputStream(this).run {
                        this.write(bytes)
                        this.close()
                    }
                }
    }
}

val shadowJar : ShadowJar by tasks.getting(ShadowJar::class) {
    configurations = listOf(project.configurations.shadow.get())
    manifest { attributes["Main-Class"] = "xland.ioutils.resourcedl.Main" }
    minimize {
        exclude (project(":"))
        exclude (project(":resourcedl-consoleapp"))
        exclude (project(":resourcedl-json-multifile"))
    }
    //relocate("org.slf4j", "xland.ioutils.resourcedl.slf4j")
        // this is because shadowJar cannot shadow providers

    exclude("c581f1b2-3d4d-40c0-95fb-8558d25aec80")
}

tasks.build.get().dependsOn(shadowJar)

publishing {
    publications.getByName("mavenJava", MavenPublication::class) {
        this.artifact(shadowJar)
    }
}
