import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.bundling.Jar

buildscript {
    fun createBuildVersion(projectVersion: String): String {
        var derivedVersion = projectVersion
        val versionWithSnapshot = projectVersion.replace("-SNAPSHOT", "")
        val buildNumber = System.getenv("TRAVIS_BUILD_NUMBER") ?: "0"
        if (project.extra["release"] == "true") {
            derivedVersion = "${versionWithSnapshot}.${buildNumber}"
        } else {
            derivedVersion = "${versionWithSnapshot}.${buildNumber}-SNAPSHOT"
        }
        println("effective project version: ${derivedVersion}")
        return derivedVersion
    }
    project.version = createBuildVersion("${project.version}")
}

val jacksonVersion: String by project
val kotlinVersion: String by project

plugins {
    idea
    java
    jacoco
    kotlin("jvm") version "1.7.0"
    id("org.sonarqube") version "2.8"
    id("maven-publish")
    signing
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    implementation("com.opencsv:opencsv:5.7.1")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:$kotlinVersion")

    testImplementation("org.mockito:mockito-core:5.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.3")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.9.3")

    testImplementation("org.junit.platform:junit-platform-commons:1.9.3")
    testImplementation("org.junit.platform:junit-platform-runner:1.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-engine:1.9.3")
}

tasks.test {
    useJUnitPlatform()
}

sonarqube {
    properties {
        property("sonar.projectName", "mounted-secrets-utils")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.projectKey", "mounted-secrets-utils")
        property("sonar.projectVersion", "${project.version}")
        property("sonar.junit.reportPaths", "${projectDir}/build/test-results/test")
        property("sonar.coverage.jacoco.xmlReportPaths", "${projectDir}/build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.coverage.exclusions", "**/R.java")
    }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.destination = file("${buildDir}/jacocoHtml")
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            enabled = true
            limit {
                minimum = "0.2".toBigDecimal()
            }
        }

        rule {
            enabled = false
            element = "BUNDLE"
            includes = listOf("com.github.starter.*")
            excludes = listOf("**/Application*")
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.1".toBigDecimal()
            }
        }
    }
}

tasks.test {
    extensions.configure(JacocoTaskExtension::class) {
        destinationFile = file("$buildDir/jacoco/jacocoTest.exec")
        classDumpDir = file("$buildDir/jacoco/classpathdumps")
    }
}

tasks.test {
    finalizedBy("jacocoTestReport")
}

tasks.check {
    dependsOn(arrayOf("jacocoTestReport", "jacocoTestCoverageVerification"))
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

val fatJar by tasks.registering(Jar::class) {
  dependsOn(JavaPlugin.CLASSES_TASK_NAME)
  from(project.configurations["runtimeClasspath"].files.map{  zipTree(it)})
  from(project.sourceSets["main"].output)
  archiveClassifier.set("uber")
  duplicatesStrategy=DuplicatesStrategy.EXCLUDE
}

artifacts {
    add("archives", sourcesJar)
    add("archives", javadocJar)
    add("archives", fatJar)
}

publishing {
    repositories {
        maven {
            var uploadUrl: String = if (project.extra["release"] == "true") {
                "${project.extra["upload.release.url"]}"
            } else {
                "${project.extra["upload.snapshot.url"]}"
            }
            url = uri(uploadUrl)
            credentials {
                username = "${project.extra["maven.username"]}"
                password = "${project.extra["maven.password"]}"
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
        }
    }
}

val scmUrl=project.extra["scm.url"]
project.publishing.publications.withType(MavenPublication::class.java).forEach { publication ->
    with(publication.pom) {
        withXml {
            val root = asNode()
            root.appendNode("name", project.name)
            root.appendNode("description", "Library to read and integrate secrets into your app")
            root.appendNode("url", scmUrl)
        }
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("${project.extra["author.handle"]}")
                name.set("${project.extra["author.name"]}")
                email.set("${project.extra["author.email"]}")
            }
        }
        scm {
            connection.set("scm:git:$scmUrl")
            developerConnection.set("scm:git:$scmUrl")
            url.set("${scmUrl}")
        }
    }
}

gradle.taskGraph.whenReady {
    if (allTasks.any { it is Sign }) {
        allprojects {
            extra["signing.keyId"] = "${project.extra["signing.keyId"]}"
            extra["signing.secretKeyRingFile"] = "${project.extra["signing.secretKeyRingFile"]}"
            extra["signing.password"] = "${project.extra["signing.password"]}"
        }
    }
}

signing {
    sign(configurations.archives.get())
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Sign>().configureEach {
    onlyIf { project.extra["release"] == "true" }
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

task("testCassandra", JavaExec::class) {
    main = "com.typeboot.DdlgenKt"
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = listOf("-Xms512m", "-Xmx512m")
    args = listOf(".cassandra.yaml")
}

task("testPostgres", JavaExec::class) {
    main = "com.typeboot.DdlgenKt"
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = listOf("-Xms512m", "-Xmx512m")
    args = listOf(".typeboot.yaml")
}

task("testApp") {
  finalizedBy ("testPostgres", "testCassandra")
}
