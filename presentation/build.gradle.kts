plugins {
    // alias(libs.plugins.akka.grpc)
}

dependencies {
    api(project(":application"))
    api(libs.bundles.circe)
}
