dependencies {
    implementation(libs.zio)
//    implementation(libs.zio.actors)
//    implementation(libs.zio.actors.persistence)
//    implementation(libs.zio.akka.cluster)
    implementation(libs.akka.cluster)
    implementation(libs.akka.cluster.sharding)
    implementation(libs.akka.persistence)
    implementation(libs.logback.classic)
    testImplementation(libs.akka.actor.testkit)
    testImplementation(libs.akka.persistence.testkit)
}
