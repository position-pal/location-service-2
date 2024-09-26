dependencies {
//    implementation(libs.zio)
//    implementation(libs.zio.actors)
//    implementation(libs.zio.actors.persistence)
//    implementation(libs.zio.akka.cluster)
    implementation(libs.akka.cluster.typed)
    implementation(libs.akka.cluster.sharding.typed)
    implementation(libs.akka.persistence.typed)
    implementation(libs.akka.persistence.r2dbc)
    implementation(libs.logback.classic)
    implementation(libs.akka.serialization.jackson)
    testImplementation(libs.akka.actor.testkit)
    testImplementation(libs.akka.persistence.testkit)
}
