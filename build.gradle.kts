plugins {
  java
}

allprojects {
  group = "ru.mrbrikster"
  version = "2.19.11"
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "java-library")

  java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  tasks {
    withType<JavaCompile>().configureEach {
      options.encoding = "UTF-8"
    }
  }

  dependencies {
    compileOnly("org.jetbrains:annotations:17.0.0")

    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    compileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")
  }
}
