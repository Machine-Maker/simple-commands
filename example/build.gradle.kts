import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

description = "An example plugin utilizing this command API"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation(project(":api"))
}

tasks {
    runServer {
        minecraftVersion("1.18.2")
    }

    java {
        withSourcesJar()
    }

    assemble {
        dependsOn(shadowJar)
    }

    processResources {
        filter(ReplaceTokens::class, "tokens" to mapOf(
            "version" to project.version
        ))
    }

    shadowJar {
        mergeServiceFiles()
        // relocate("me.machinemaker.commands", "me.machinemaker.commands.example.libs.commands") {
        //     exclude("me.machinemaker.commands.example.*")
        // }
    }
}
