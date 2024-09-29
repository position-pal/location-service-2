dependencies {
    api(project(":presentation"))
    implementation(libs.bundles.http4s)
    implementation(libs.log4cats.slf4j)
    implementation(libs.akka.cluster.typed)
    implementation(libs.akka.cluster.sharding.typed)
    implementation(libs.akka.persistence.typed)
    implementation(libs.akka.persistence.r2dbc)
    implementation(libs.akka.serialization.jackson)
    implementation(libs.logback.classic)
    testImplementation(libs.akka.actor.testkit)
    testImplementation(libs.akka.persistence.testkit)
    implementation("com.fasterxml.jackson.module:jackson-module-scala_3:2.18.0")
}
