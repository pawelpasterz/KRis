import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import org.sonarqube.gradle.SonarQubeTask

plugins {
    kotlin("jvm")
    id("org.kordamp.gradle.kotlin-project")
    java
    id("org.kordamp.gradle.integration-test") apply false
    id("org.sonarqube")
    id("io.gitlab.arturbosch.detekt")
    id("org.ajoberstar.reckon")
}

config {
    release = rootProject.findProperty("release").toString().toBoolean()

    info {
        name = "JRis"
        vendor = "Private"
        description = "Library for reading/writing RIS files"
        inceptionYear = "2017"
        organization {
            url = "https://github.com/ursjoss/JRis.git"
        }
        links {
            scm = "https://github.com/ursjoss/JRis.git"
        }
        people {
            person {
                id = "fastluca"
                name = "Gianluca Colaianni"
                roles = listOf("developer")
            }
            person {
                id = "ursjoss"
                name = "Urs Joss"
                roles = listOf("developer")
            }
        }
    }

    licensing {
        enabled = false
        licenses {
            license {
                id = "MIT"
            }
        }
    }
}

allprojects {
    repositories {
        mavenLocal()
        jcenter()
        mavenCentral()
    }

    tasks {
        val deleteOutFolderTask by registering(Delete::class) {
            delete("out")
        }
        named("clean") {
            dependsOn(deleteOutFolderTask)
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply<JavaPlugin>()
    apply<IdeaPlugin>()
    apply<JacocoPlugin>()
    apply<DetektPlugin>()

    dependencies {
        implementation(Lib.kotlin("stdlib-jdk8"))
        implementation(Lib.kotlin("reflect"))
        implementation(Lib.kotlinx("coroutines-core"))

        testImplementation(Lib.junit5("api"))
        testImplementation(Lib.spek("dsl-jvm"))
        testImplementation(Lib.mockk())
        testImplementation(Lib.kluent())
        testImplementation(Lib.assertJ())

        testRuntimeOnly(Lib.junit5("engine"))
        testRuntimeOnly(Lib.spek("runner-junit5"))
    }

    tasks {
        withType<Test> {
            @Suppress("UnstableApiUsage")
            useJUnitPlatform {
                includeEngines("junit-jupiter", "spek2")
            }
        }
    }

}

reckon {
    scopeFromProp()
    stageFromProp("beta", "rc", "final")
}

private val detektReportRoot = "${rootProject.buildDir}/reports/detekt"
val detektXml = "$detektReportRoot/detekt.xml"
val detektHtml = "$detektReportRoot/detekt.html"

tasks {
    withType<Detekt> {
        buildUponDefaultConfig = true
        failFast = false
        file("${rootProject.projectDir}/detekt-config.yml").takeIf { it.exists() }?.let {
            config.setFrom(it)
        }
        file("${rootProject.projectDir}/detekt-baseline.xml").takeIf { it.exists() }?.let {
            baseline.set(it)
        }
        source = fileTree(rootProject.projectDir)
        include("**/*.kt")
        include("**/*.kts")
        exclude("**/resources/")
        exclude("**/build/")
        reports {
            html {
                enabled = true
                destination = file(detektHtml)
            }
            xml {
                enabled = true
                destination = file(detektXml)
            }
        }
    }
    withType<SonarQubeTask> {
        description = "Push jacoco analysis to sonarcloud."
        group = "Verification"
        subprojects.forEach {
            dependsOn("${it.path}:allTests")
            dependsOn("${it.path}:jacocoTestReport")
        }
        dependsOn("detekt")
        dependsOn("jacocoRootReport")
    }
}


sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectKey", "ursjoss_JRis")
        property("sonar.organization", "ursjoss-github")
        property("sonar.kotlin.detekt.reportPaths", detektXml)
    }
}
