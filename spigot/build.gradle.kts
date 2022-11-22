plugins {
  id("com.github.johnrengelman.shadow") version "6.1.0"
  id("xyz.jpenilla.run-paper") version "1.0.6"
}

tasks {
  shadowJar {
    archiveFileName.set("Chatty.jar")
    destinationDirectory.set(File(rootProject.projectDir.absolutePath + "/build/libs"))
    relocate("ru.mrbrikster.baseplugin", "${rootProject.group}.${rootProject.name}.shaded.baseplugin")
    relocate("org.bstats", "${rootProject.group}.${rootProject.name}.shaded.metrics")
    relocate("com.google.gson", "${rootProject.group}.${rootProject.name}.shaded.gson")
  }

  build {
    dependsOn(shadowJar)
  }

  runServer {
    minecraftVersion("1.19")
  }
}

tasks {
  processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = Charsets.UTF_8.name()
    filesMatching("plugin.yml") {
      expand(props)
    }
  }
}

dependencies {
  api(project(":api"))
  api("com.github.Brikster:BasePlugin:v1.8")
  api("com.google.code.gson:gson:2.8.9")
  api("org.bstats:bstats-bukkit:2.2.1")

  compileOnly("net.milkbowl.vault:VaultAPI:1.7")
  compileOnly("me.clip:placeholderapi:2.10.6")
  compileOnly("commons-io:commons-io:2.7")
}
