plugins {
    // alias(libs.plugins.akka.grpc)
}

dependencies {
    api(project(":application"))
    api(libs.bundles.circe)
    api("io.bullet:borer-core_3:1.14.1")
    api("io.bullet:borer-derivation_3:1.14.1")
    api("io.bullet:borer-compat-akka_3:1.14.1")
    api("io.bullet:borer-compat-circe_3:1.14.1")
    api("io.bullet:borer-compat-scodec_3:1.14.1")
}
