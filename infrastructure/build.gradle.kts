dependencies {
    api(project(":presentation"))
    implementation(libs.bundles.http4s)
    implementation(libs.log4cats.slf4j)
    implementation(libs.akka.cluster.typed)
    implementation(libs.akka.cluster.sharding.typed)
    implementation(libs.akka.persistence.typed)
    implementation(libs.logback.classic)
    testImplementation(libs.akka.actor.testkit)
    testImplementation(libs.akka.persistence.testkit)
}
