/*
 * @copyright@
 */

import zakadabar.gradle.isPublishing
import zakadabar.gradle.manifestAndDokka
import zakadabar.gradle.config
import java.util.*

plugins {
    kotlin("multiplatform") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.5.30"

    id("org.jetbrains.dokka") version "1.4.32"

    id("com.github.johnrengelman.shadow") version "7.0.0"
    application

    id("com.palantir.docker") version "0.25.0"

    signing
    `maven-publish`

    id("zk-build-tasks") apply false
}

// ---- ZK-CUSTOMIZE-START -----------------------------------------------------

//    ↓ ↓ ↓ ↓   READ COMMENTS  ↓ ↓ ↓ ↓

// -----------------------------------------------------------------------------
// You don't have to modify anything above this
// -----------------------------------------------------------------------------

group = "my.application.group"
version = "2021.9.28"

tasks.register<zakadabar.gradle.CustomizeTask>("zkCustomize") {

    packageName = "my.pkg.name" // the package your code resides in

    projectPath = "" // "spxbhuhb/zakadabar-application-template"
    projectUrl = "" // "https://github.com/$projectPath"

    license = "" // "Apache 2.0"
    licenseUrl = "" // "https://www.apache.org/licenses/LICENSE-2.0.txt"

    organizationName = "" // "Simplexion Kft."

    copyright = "Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license."

    // the title of your application, this is the title of the web pace
    // also, if you publish to maven this is the description of your package

    applicationTitle = "My Application"

    defaultLocale = "en"

    sqlDriver = "org.h2.Driver"
    sqlDatabase = project.name
    sqlUrl = "jdbc:h2:./app/var/$sqlDatabase"
    sqlUser = "local"
    sqlPassword = UUID.randomUUID().toString()

    dockerImageName = project.name

    dockerSqlDriver = "org.postgresql.Driver"
    dockerSqlDatabase = project.name
    dockerSqlUrl = "jdbc:postgresql://localhost/$sqlDatabase"
    dockerSqlUser = "postgres"
    dockerSqlPassword = sqlPassword

}

// -----------------------------------------------------------------------------
// You don't have to modify anything below this
// -----------------------------------------------------------------------------

// ---- ZK-CUSTOMIZE-END -------------------------------------------------------

val isSnapshot = version.toString().contains("SNAPSHOT")

val stackVersion by extra { "2021.9.15" }
val datetimeVersion = "0.2.1"

// in TeamCity we can use the build number to find the generated docker image
println("##teamcity[buildNumber '${project.version}']")

repositories {
    mavenCentral()
    if (stackVersion.contains("SNAPSHOT")) {
        maven {
            url = project.uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenLocal()
    }
}

application {
    mainClass.set("zakadabar.core.server.ServerKt")
}

noArg {
    annotation("kotlinx.serialization.Serializable")
}

kotlin {

    jvm {
        withJava()
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js {
        browser()
    }

    sourceSets["commonMain"].dependencies {
        implementation("hu.simplexion.zakadabar:core:$stackVersion")
        implementation("hu.simplexion.zakadabar:accounts:$stackVersion")
        implementation("hu.simplexion.zakadabar:blobs:$stackVersion")
        implementation("hu.simplexion.zakadabar:i18n:$stackVersion")
    }

    sourceSets["jvmMain"].dependencies {
        implementation("com.h2database:h2:1.4.200")
    }
}

// -----------------------------------------------------------------------------
// Built a fat JAR and server distribution package
// -----------------------------------------------------------------------------

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    // seems like this does not work - minimize()
}

apply(plugin = "zk-build-tasks")

// -----------------------------------------------------------------------------
// Build a docker image
// -----------------------------------------------------------------------------

docker {

    dependsOn(tasks["zkBuild"], tasks["zkDockerPrepare"], tasks["zkDockerCopy"])

    name = "${project.name}:${project.version}"

    pull(true)
    setDockerfile(file("Dockerfile"))

    buildArgs(mapOf(
        "BUILD_NUMBER" to Date().toString(),
        "STACK_VERSION" to stackVersion
    ))


}

// -----------------------------------------------------------------------------
// Publish artifacts to a Maven repository
// -----------------------------------------------------------------------------

if (project.isPublishing) {

    manifestAndDokka(tasks)

    signing { config(publishing.publications) }

    publishing {
        config(project)

        publications.withType<MavenPublication>().all {
            config(tasks["javadocJar"], "@applicationTitle@")
        }
    }

}