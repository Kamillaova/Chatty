@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
  dependencyResolutionManagement {
    repositories {
      mavenCentral()
      maven("https://papermc.io/repo/repository/maven-public/") {
        content {
          includeGroup("net.md-5")
          includeGroup("com.mojang")
          includeGroupByRegex("io.papermc.*")
          includeGroupByRegex("com.destroystokyo.*")
        }
        maven("https://repo.codemc.io/repository/nms/") {
          content { includeGroupByRegex("com.destroystokyo.*") }
        }
        maven("https://jitpack.io") {
          content { includeGroupByRegex("com.github.*") }
        }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
          content { includeGroup("me.clip") }
        }
      }
    }
  }
}

rootProject.name = "chatty"
