plugins {
  `maven-publish`
}

publishing {
  publications {
    create("maven", MavenPublication::class) {
      from(components["java"])
      artifactId = "chatty-api"
    }
  }
}
