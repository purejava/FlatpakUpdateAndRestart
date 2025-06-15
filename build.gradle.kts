import net.thebugmc.gradle.sonatypepublisher.PublishingType.*

plugins {
    id("java-library")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
    id("maven-publish")
    id("signing")
}

group = "org.purejava"
version = "1.0-SNAPSHOT"
description = "A Java library for updating Flatpak apps."

val sonatypeUsername: String = System.getenv("SONATYPE_USERNAME") ?: ""
val sonatypePassword: String = System.getenv("SONATYPE_PASSWORD") ?: ""

java {
    java.sourceCompatibility = JavaVersion.VERSION_24
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.com.github.hypfvieh.dbus.java.core)
    api(libs.com.github.hypfvieh.dbus.java.transport.native.unixsocket)
    api(libs.org.slf4j.slf4j.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
    testImplementation(libs.org.slf4j.slf4j.simple)
    testRuntimeOnly(libs.org.junit.platform.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    filter {
        includeTestsMatching("UpdatePortalTest")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("FlatpakUpdateAndRestart")
                description.set("A Java library for updating Flatpak apps.")
                url.set("https://github.com/purejava/FlatpakUpdateAndRestart")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("purejava")
                        name.set("Ralph Plawetzki")
                        email.set("ralph@purejava.org")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/purejava/FlatpakUpdateAndRestart.git")
                    developerConnection.set("scm:git:ssh://github.com/purejava/FlatpakUpdateAndRestart.git")
                    url.set("https://github.com/purejava/FlatpakUpdateAndRestart/tree/main")
                }

                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/purejava/FlatpakUpdateAndRestart/issues")
                }
            }
        }
    }
}

centralPortal {
    publishingType.set(USER_MANAGED)

    username.set(sonatypeUsername)
    password.set(sonatypePassword)

    // Configure POM metadata
    pom {
        name.set("FlatpakUpdateAndRestart")
        description.set("A Java library for updating Flatpak apps.")
        url.set("https://github.com/purejava/FlatpakUpdateAndRestart")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("purejava")
                name.set("Ralph Plawetzki")
                email.set("ralph@purejava.org")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/purejava/FlatpakUpdateAndRestart.git")
            developerConnection.set("scm:git:ssh://github.com/purejava/FlatpakUpdateAndRestart.git")
            url.set("https://github.com/purejava/FlatpakUpdateAndRestart/tree/main")
        }
        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/FlatpakUpdateAndRestart/issues")
        }
    }
}

if (!version.toString().endsWith("-SNAPSHOT")) {
    signing {
        useGpgCmd()
        sign(configurations.runtimeElements.get())
        sign(publishing.publications["mavenJava"])
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    isFailOnError = false
    if (JavaVersion.current().isJava9Compatible) {
        (options as? StandardJavadocDocletOptions)?.addBooleanOption("html5", true)
    }
    (options as? StandardJavadocDocletOptions)?.encoding = "UTF-8"
}
