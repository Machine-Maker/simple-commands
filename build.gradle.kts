plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "me.machinemaker"
description = "Parent for this simple commands API"

allprojects {
    project.version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin="java-library")
    apply(plugin="maven-publish")
    apply(plugin="signing")

    group = "me.machinemaker.commands"

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    publishing {
        repositories {
            maven {
                name = "snapshots"
                url = uri("https://oss.sonatype.org/content/repositories/snapshots")
                credentials(PasswordCredentials::class)
            }
            maven {
                name = "central"
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            }
        }

        publications {
            create<MavenPublication>("maven") {
                pom {
                    licenses {
                        license {
                            name.set("GNU GENERAL PUBLIC LICENSE")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                        }
                    }

                    developers {
                        developer {
                            id.set("Machine Maker")
                            name.set("Jake Potrebic")
                            email.set("machine@machinemaker.me")
                        }
                    }
                }

                groupId = "me.machinemaker"
                artifactId = project.name
                version = project.version.toString()

                from(components["java"])
            }
        }
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["maven"])
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }

        compileJava {
            options.release.set(17)
            options.encoding = Charsets.UTF_8.name()
        }

        javadoc {
            options.encoding = Charsets.UTF_8.name()
        }

        processResources {
            filteringCharset = Charsets.UTF_8.name()
        }
    }
}

