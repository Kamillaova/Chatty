plugins {
  java
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "ru.mrbrikster.chatty"
version = "2.19.11"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
  compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")

  compileOnly("org.jetbrains:annotations:23.0.0")

  implementation("com.github.Brikster:BasePlugin:v1.8")
  implementation("net.kyori:adventure-platform-bukkit:4.1.2")
  implementation("net.kyori:adventure-text-serializer-gson:4.11.0")
  implementation("net.kyori:adventure-text-serializer-gson-legacy-impl:4.11.0")

  compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
  compileOnly("me.clip:placeholderapi:2.11.2")
}

tasks {
  jar { enabled = false }
  assemble { dependsOn(shadowJar) }
  processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = Charsets.UTF_8.name()
    filesMatching("plugin.yml") {
      expand(props)
    }
  }
  withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
  }
  shadowJar {
    fun autoRelocate(vararg pkgs: String) {
      pkgs.forEach { relocate(it, "${project.group}.shaded.$it") }
    }

    autoRelocate(
      "ru.mrbrikster.baseplugin",
      "net.kyori",
    )

    mergeServiceFiles()

    exclude("**/module-info.class", "META-INF/maven/**")

    manifest {
      attributes("Multi-Release" to "true")
    }

    archiveFileName.set("${project.name}.jar")
  }
  withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
  }
}
