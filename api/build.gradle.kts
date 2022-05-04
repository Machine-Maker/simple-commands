plugins {
    `java-library`
}

description = "A simple commands API"

repositories {
    maven("https://libraries.minecraft.net")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    api("com.mojang:brigadier:1.0.18")
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-mojangapi:1.18.2-R0.1-SNAPSHOT")
}

