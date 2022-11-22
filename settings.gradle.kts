@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.org/repository/maven-public")
  }
}

rootProject.name = "chatty"

include(
  "api",
  "spigot"
)
