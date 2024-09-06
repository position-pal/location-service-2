dependencies {
    api(project(":presentation"))
    implementation(libs.bundles.http4s)
    implementation(libs.log4cats.slf4j)
    implementation(libs.akka.cluster)
    implementation(libs.akka.cluster.sharding)
}
