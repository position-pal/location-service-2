dependencies {
    api(project(":application"))
    api(libs.bundles.circe)
    api(libs.bundles.borer)
    implementation("com.typesafe.akka:akka-actor_3:2.9.5")
}
