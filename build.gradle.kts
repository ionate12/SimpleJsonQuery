plugins {
    kotlin("multiplatform") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    `maven-publish`
}

group = "com.github.ionate12"  // JitPack format: com.github.USERNAME
version = "1.0.0"

repositories {
    mavenCentral()
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
        browser {
            testTask {
                useMocha()
            }
        }
        nodejs {
            testTask {
                useMocha()
            }
        }
    }

    // Native targets
    linuxX64()
    mingwX64()
    macosX64()
    macosArm64()
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

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.10.1")
            }
        }

        val jsMain by getting
        val jsTest by getting

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val nativeTest by creating {
            dependsOn(commonTest)
        }

        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val linuxX64Test by getting {
            dependsOn(nativeTest)
        }

        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        val mingwX64Test by getting {
            dependsOn(nativeTest)
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
        val macosX64Test by getting {
            dependsOn(nativeTest)
        }

        val macosArm64Main by getting {
            dependsOn(nativeMain)
        }
        val macosArm64Test by getting {
            dependsOn(nativeTest)
        }

        val iosArm64Main by getting {
            dependsOn(nativeMain)
        }
        val iosArm64Test by getting {
            dependsOn(nativeTest)
        }

        val iosX64Main by getting {
            dependsOn(nativeMain)
        }
        val iosX64Test by getting {
            dependsOn(nativeTest)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(nativeMain)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(nativeTest)
        }
    }
}

// Publishing configuration for JitPack
publishing {
    publications {
        withType<MavenPublication> {
            groupId = "com.github.ionate12"
            version = project.version.toString()

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
                    developerConnection.set("scm:git:ssh://github.com/ionate12/SimpleJsonQuery.git")
                    url.set("https://github.com/ionate12/SimpleJsonQuery")
                }
            }
        }
    }
}

// Required for Gradle metadata
tasks.withType<GenerateModuleMetadata> {
    enabled = true
}
