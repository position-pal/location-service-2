import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.remove

plugins {
    id("com.github.imflog.kafka-schema-registry-gradle-plugin") version "2.1.1"
    id("com.google.protobuf") version "0.9.4"
}

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://packages.confluent.io/maven/")
        maven("https://jitpack.io")
    }
}

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
    implementation("org.apache.kafka:kafka-streams-scala_2.13:3.8.0")
    implementation("org.apache.kafka:kafka-clients:3.8.0")
    implementation("org.typelevel:protoc-gen-fs2-grpc:2.7.20")
    implementation("com.thesamet.scalapb:scalapb-runtime_3:1.0.0-alpha.1")
    implementation("com.thesamet.scalapb:scalapb-runtime-grpc_3:1.0.0-alpha.1")
    implementation("io.grpc:grpc-core:1.68.0")
    implementation("io.grpc:grpc-stub:1.68.0")
    implementation("io.grpc:grpc-protobuf:1.68.0")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.1"
    }
    plugins {
        id("scalapb") {
            artifact = "com.thesamet.scalapb:protoc-gen-scala:1.0.0-alpha.1:unix@sh"
        }
        id("fs2grpc") {
            artifact = "org.typelevel:protoc-gen-fs2-grpc:2.7.20:unix@sh"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                remove("java")
            }
            it.plugins {
                id("scalapb") {}
                id("fs2grpc") {
                    option("serviceSuffix=Fs2Grpc")
                }
            }
        }
    }
}

schemaRegistry {
    url = "http://localhost:8081"
    pretty = true
    register {
        subject(
            inputSubject = "sampled-locations-value",
            file = "experiments/src/main/proto/location.proto",
            type = "PROTOBUF"
        )
    }
}

scalaExtras {
    qa {
        allWarningsAsErrors = false
    }
}
