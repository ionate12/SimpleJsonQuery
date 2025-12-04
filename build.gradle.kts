import java.util.Properties

plugins {
    kotlin("multiplatform") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("maven-publish")
    id("signing")
}

group = "io.github.ionate12"
version = "1.0.1"

repositories {
    mavenCentral()
}

// Load local.properties
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localPropertiesFile.reader().use { localProperties.load(it) }
}

fun getPropertyValue(key: String): String? {
    return localProperties.getProperty(key)
        ?: findProperty(key) as String?
        ?: System.getenv(key.uppercase().replace(".", "_"))
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    // Tier 1 Native targets (most stable for Maven Central)
    linuxX64()
    macosX64()
    macosArm64()

    // iOS targets
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.10.1")
            }
        }
    }
}

// Explicitly configure each publication
publishing {
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://central.sonatype.com/api/v1/publisher/upload?name=${project.group}&publishingType=AUTOMATIC")

            credentials {
                username = getPropertyValue("ossrhUsername")
                password = getPropertyValue("ossrhPassword")
            }
        }
    }

    publications.withType<MavenPublication> {
        // Configure artifact naming
        artifactId = when (name) {
            "kotlinMultiplatform" -> "simple-json-query"
            "metadata" -> "simple-json-query-metadata"
            else -> "simple-json-query-$name"
        }

        pom {
            name.set("Simple JSON Query")
            description.set("A lightweight JSON query language for Kotlin Multiplatform - similar to JMESPath but simpler")
            url.set("https://github.com/ionate12/SimpleJsonQuery")

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("ionate12")
                    name.set("Minh Khoi Mai")
                    email.set("mmkhoi.uel@gmail.com")
                }
            }

            scm {
                connection.set("scm:git:https://github.com/ionate12/SimpleJsonQuery.git")
                developerConnection.set("scm:git:ssh://git@github.com/ionate12/SimpleJsonQuery.git")
                url.set("https://github.com/ionate12/SimpleJsonQuery")
            }
        }
    }
}

// Signing configuration
signing {
    val signingKey = getPropertyValue("signingKey")
    val signingPassword = getPropertyValue("signingPassword")

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}
