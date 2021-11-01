/*
 * Copyright © 2020, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package zakadabar.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")

abstract class CustomizeTask : DefaultTask() {

    private val mapping = mutableMapOf<String, String?>()

    @Input
    var projectName: String?

    @Input
    var projectPath: String?

    @Input
    var projectUrl: String?

    @Input
    var license: String?

    @Input
    var licenseUrl: String?

    @Input
    var organizationName: String?

    @Input
    var packageName: String?

    @Input
    var applicationTitle: String?

    @Input
    var defaultLocale: String?

    @Input
    var copyright: String

    @Input
    var sqlDriver: String?

    @Input
    var sqlUrl: String? = null

    @Input
    var sqlDatabase: String?

    @Input
    var sqlUser: String?

    @Input
    var sqlPassword: String?

    @Input
    var dockerImageName: String?

    @Input
    var dockerSqlDriver: String?

    @Input
    var dockerSqlUrl: String? = null

    @Input
    var dockerSqlDatabase: String?

    @Input
    var dockerSqlUser: String? = null

    @Input
    var dockerSqlPassword: String? = null

    init {
        group = "zakadabar"

        projectName = project.name
        packageName = null

        projectPath = ""
        projectUrl = ""

        license = ""
        licenseUrl = ""

        organizationName = ""

        applicationTitle = project.name.capitalize()

        defaultLocale = "en"
        copyright = "Copyright © 2020, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license."

        sqlDriver = "org.h2.Driver"
        sqlDatabase = project.name
        sqlUser = "local"
        sqlPassword = UUID.randomUUID().toString()

        dockerImageName = project.name

        dockerSqlDriver = "org.postgresql.Driver"
        dockerSqlDatabase = project.name
        dockerSqlUser = "postgres"
        dockerSqlPassword = sqlPassword
    }

    private var rootDir: String = this.project.rootDir.absolutePath

    private val packageDir: String
        get() = packageName !!.replace(".", "/")

    fun Path.readString(): String =
        Files.readAllBytes(this).decodeToString()

    @TaskAction
    fun customizeProject() {
        if (this.project.rootProject.name == "zakadabar-application-template") {
            throw IllegalStateException("You have to change name of the project in settings.gradle.kts!")
        }

        if (packageName == null) {
            throw IllegalStateException("You have to change the base package name in the customize task in build.gradle.kts!")
        }

        if (packageName !!.endsWith(".")) {
            throw IllegalArgumentException("The package name in gradle.properties must not end with a dot!")
        }

        if (! File(rootDir, "src/commonMain/kotlin/zakadabar/template").exists()) {
            throw IllegalArgumentException("Customization must not be run more than once!")
        }

        mapping["projectName"] = projectName
        mapping["packageName"] = packageName

        mapping["projectPath"] = projectPath
        mapping["projectUrl"] = projectUrl
        mapping["license"] = license
        mapping["licenseUrl"] = licenseUrl
        mapping["organizationName"] = organizationName

        mapping["applicationTitle"] = applicationTitle

        mapping["defaultLocale"] = defaultLocale

        mapping["sqlDriver"] = sqlDriver
        mapping["sqlDatabase"] = sqlDatabase
        mapping["sqlUrl"] = sqlUrl ?: "jdbc:h2:./app/var/$sqlDatabase"
        mapping["sqlUser"] = sqlUser
        mapping["sqlPassword"] = sqlPassword

        mapping["dockerImageName"] = dockerImageName

        mapping["dockerSqlDriver"] = dockerSqlDriver
        mapping["dockerSqlDatabase"] = dockerSqlDatabase
        mapping["dockerSqlUrl"] = dockerSqlUrl ?: "jdbc:postgresql://db/$dockerSqlDatabase"
        mapping["dockerSqlUser"] = dockerSqlUser
        mapping["dockerSqlPassword"] = dockerSqlPassword

        mapping["copyright"] = copyright

        println("Customising: $projectName / $packageName")

        sourceSet("commonMain")
        sourceSet("jsMain")
        sourceSet("jvmMain")

        packageNames()

        index()
        strings()

        map("buildSrc/src/main/kotlin/zakadabar/gradle/publishing.kt")

        map("template/app/etc/stack.server.yaml")
        map("template/app/etc/stack.server-docker.yaml")
        map("template/docker/Dockerfile")
        map("template/docker/docker-compose.yml")

        buildGradleKts()

        println("Customisation: done")
    }

    private fun sourceSet(targetName: String) {
        mkdir("src/$targetName/kotlin/$packageDir")
        moveAll("src/$targetName/kotlin/zakadabar/template", "src/$targetName/kotlin/$packageDir")
        delete("src/$targetName/kotlin/zakadabar/template")
        delete("src/$targetName/kotlin/zakadabar/")
    }

    private fun mkdir(dirs: String) {
        val path = Paths.get(rootDir, dirs)
        Files.createDirectories(path)
        println("    create: $path")
    }

    private fun moveAll(from: String, to: String) {
        val fromPath = Paths.get(rootDir, from)
        Files.list(fromPath).forEach {
            val toPath = Paths.get(rootDir, to).resolve(it.fileName)
            Files.move(it, toPath)
            println("    move: $it  >  $toPath")
        }
    }

    private fun delete(target: String) {
        val path = Paths.get(rootDir, target)
        if (Files.list(path).count() != 0L) {
            logger.warn("Directory $path is not empty, skipping delete.")
            return
        }
        Files.delete(path)
        println("    delete: $path")
    }

    private fun packageNames() {
        File(rootDir, "src").walk().forEach {
            if (! it.isFile) return@forEach
            if (! it.name.endsWith(".kt")) return@forEach

            val content = it.toPath().readString()

            val newContent = content
                .replace("package zakadabar.template", "package $packageName")
                .replace("import zakadabar.template", "import $packageName")
                .replace("@copyright@", copyright)

            Files.write(it.toPath(), newContent.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)

            println("    package name: ${it.absolutePath}")
        }
    }

    private fun index() {
        val path = Paths.get(rootDir, "src/jsMain/resources/index.html")
        val content = path.readString()

        val newContent = content
            .replace("<title>template</title>", "<title>${applicationTitle}</title>")
            .replace("/zakadabar-application-template.js", "/${project.rootProject.name}.js")

        Files.write(path, newContent.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)

        println("    page title: $path")
        println("    JS file name: $path")
    }

    private fun strings() {
        val path = Paths.get(rootDir, "src/commonMain/kotlin/$packageDir/resources/AppStrings.kt")
        val content = path.readString()

        val newContent = content
            .replace("by \"template\"", "by \"${applicationTitle}\"")

        Files.write(path, newContent.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)

        println("    application name: $path")
    }

    private fun map(relPath: String) {

        val path = Paths.get(rootDir, relPath)

        var content = path.readString()

        mapping.forEach {
            val value = it.value ?: return@forEach
            content = content.replace("@${it.key}@", value)
        }

        Files.write(path, content.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)

        println("    map: $path")
    }

    private fun buildGradleKts() {

        val path = Paths.get(rootDir, "build.gradle.kts")

        var content = path.readString()

        mapping["copyright"]?.let { content = content.replace("@copyright@", it) }
        mapping["applicationTitle"]?.let { content = content.replace("@applicationTitle@", it) }

        val start = "// ---- ZK-CUSTOMIZE-START -----------------------------------------------------"
        val end = "// ---- ZK-CUSTOMIZE-END -------------------------------------------------------"

        val before = content.substringBefore(start)
        val after = content.substringAfter(end)

        val groupPattern = Regex("""group\s*=\s*.*""")
        val versionPattern = Regex("""version\s*=\s*.*""")

        val custom = content.substringAfter(start).substringBefore(end).split("\n")
        val group = custom.first { it.trim().matches(groupPattern) }
        val version = custom.first { it.trim().matches(versionPattern) }

        content = "$before$group\n$version$after"

        Files.write(path, content.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)

        println("    map: $path")
    }
}