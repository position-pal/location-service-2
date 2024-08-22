import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    id("scala")
    alias(libs.plugins.scala.extras)
}

class DotenvConfiguration(private val fileName: String = DEFAULT_ENV_FILE_NAME) {

    fun environmentVariables(): Map<String, String> =
        rootDir.resolve(dotenv.fileName)
            .takeIf { it.exists() }
            ?.readLines()
            ?.filter { it.isNotBlank() && !it.startsWith(COMMENT_SYMBOL) }
            ?.associate { it.split(KEY_VALUE_SEPARATOR).let { (key, value) -> key to value } }
            ?: emptyMap()

    companion object {
        private const val DEFAULT_ENV_FILE_NAME = ".env"
        private const val COMMENT_SYMBOL = "#"
        private const val KEY_VALUE_SEPARATOR = "="
    }
}
val dotenv = DotenvConfiguration()

allprojects {
    group = "io.github.positionpal"

    with(rootProject.libs.plugins) {
        apply(plugin = "java-library")
        apply(plugin = "scala")
        // TODO: generated code by protobuf should be excluded from qa checks
        if (name != "presentation") {
            apply(plugin = scala.extras.get().pluginId)
        }
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.akka.io/maven") }
    }

    with(rootProject.libs) {
        dependencies {
            implementation(scala.library)
            implementation(bundles.cats)
            testImplementation(bundles.scala.testing)
        }
    }

    tasks.test {
        useJUnitPlatform {
            includeEngines("scalatest")
        }
        testLogging {
            showCauses = true
            showStackTraces = true
            events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.STARTED, TestLogEvent.STANDARD_OUT)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    tasks.withType<JavaExec> {
        dotenv.environmentVariables().forEach { environment(it.key, it.value) }
    }

    tasks.withType<Test> {
        dotenv.environmentVariables().forEach { environment(it.key, it.value) }
    }
}
