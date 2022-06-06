plugins {
    `java-library`
}

description = "A simple commands API"

repositories {
    maven("https://libraries.minecraft.net")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnlyApi("com.mojang:brigadier:1.0.18")
    compileOnlyApi("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnlyApi("io.papermc.paper:paper-mojangapi:1.18.2-R0.1-SNAPSHOT") {
        exclude("com.mojang", "brigadier")
    }
    runtimeOnly(project(":nms", configuration = "reobf"))

    testImplementation("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
}

