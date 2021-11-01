import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/*
 * Copyright © 2020, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

// -----------------------------------------------------------------------------
//  These tasks update the template/zkBuild/zkBuild.properties file with the
//  current versions and names.
// -----------------------------------------------------------------------------

val syncBuildInfo by tasks.registering(Copy::class) {
    from("$projectDir/template/zkBuild")
    inputs.property("version", project.version)
    filter { line: String ->
        line.replace("@version@", "${project.version}")
            .replace("@projectName@", project.name)
            .replace("@stackVersion@", project.extra["stackVersion"].toString())
    }
    into("$projectDir/src/jvmMain/resources")
}

tasks.named<Copy>("jvmProcessResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks["compileKotlinJvm"].dependsOn(syncBuildInfo)

// -----------------------------------------------------------------------------
//  These tasks compose a directory structure that contains a whole,
//  deployable application with scripts, configurations, jar files, etc.
// -----------------------------------------------------------------------------

val distDir = "$buildDir/app/${project.name}-$version-server"

val copyAppStruct by tasks.registering(Copy::class) {
    from("$projectDir/template/app")
    into(distDir)
    include("**")
    exclude("**/.gitignore")

    filter { line: String ->
        line.replace("@version@", "${project.version}")
    }
}

val copyAppLib by tasks.registering(Copy::class) {
    from("$buildDir/libs")
    into("$distDir/lib")
    include("${project.name}-${project.version}-all.jar")
}

val copyAppIndex by tasks.registering(Copy::class) {
    from("$buildDir/distributions")
    into("$distDir/var/static")
    include("index.html")
    filter { line: String ->
        line.replace("""src="/${project.name}.js"""", """src="/${project.name}-${project.version}.js"""")
    }
}

val copyAppStatic by tasks.registering(Copy::class) {
    from("$buildDir/distributions")
    into("$distDir/var/static")
    include("**")

    exclude("index.html")
    exclude("*.tar")
    exclude("*.zip")

    rename("${project.name}.js", "${project.name}-${project.version}.js")
    dependsOn(tasks["jsBrowserProductionWebpack"], tasks["jsBrowserDistributeResources"])
}

val copyAppUsr by tasks.registering(Copy::class) {
    from("$projectDir")
    into("$distDir/usr")
    include("README.md")
    include("LICENSE.txt")
}

val zkBuild by tasks.registering(Zip::class) {
    group = "zakadabar"

    dependsOn(tasks["shadowJar"], copyAppStruct, copyAppLib, copyAppStatic, copyAppIndex, copyAppUsr)

    archiveFileName.set("${project.name}-${project.version}-server.zip")
    destinationDirectory.set(file("$buildDir/app"))

    from(distDir)
}

// -----------------------------------------------------------------------------
//  These tasks prepare the build of the docker image that runs the application.
// -----------------------------------------------------------------------------

abstract class DockerPrepareTask : DefaultTask() {

    private val rootDir: String = this.project.rootDir.absolutePath
    private val buildDir: String = this.project.rootProject.buildDir.path
    private val version: String = project.rootProject.version.toString()

    @TaskAction
    private fun prepareDocker() {
        Files.createDirectories(Paths.get(buildDir, "docker/local"))
        dockerFile()
        dockerCompose()
    }

    private fun dockerFile() {
        val fromPath = Paths.get(rootDir, "template/docker/Dockerfile")
        val toPath = Paths.get(buildDir, "docker/Dockerfile")

        val content = Files.readAllBytes(fromPath).decodeToString()

        val newContent = content
            .replace("@version@", version)

        Files.write(toPath, newContent.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    private fun dockerCompose() {
        val fromPath = Paths.get(rootDir, "template/docker/docker-compose.yml")
        val toPath = Paths.get(buildDir, "app/docker-compose-$version.yml")

        val content = Files.readAllBytes(fromPath).decodeToString()

        val newContent = content
            .replace("@version@", "/$version")

        Files.write(toPath, newContent.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

}


val zkDockerPrepare by tasks.register<DockerPrepareTask>("zkDockerPrepare") {
    group = "zakadabar"
}

val zkDockerCopy by tasks.registering(Copy::class) {
    from("$buildDir/app/${project.name}-$version-server")
    into("$buildDir/docker/local/${project.name}")
    include("**")
}

val zkDocker by tasks.creating(Task::class) {
    group = "zakadabar"
    dependsOn(tasks.getByName("docker"))
}