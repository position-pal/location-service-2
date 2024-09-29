import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    id("scala")
    alias(libs.plugins.scala.extras)
    alias(libs.plugins.cucumber.jvm)
}

class DotenvConfiguration(private val fileName: String = DEFAULT_ENV_FILE_NAME) {

    fun environmentVariables(): Map<String, String> =
        rootDir.resolve(fileName)
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
val dotenvConfig = DotenvConfiguration()

allprojects {
    group = "io.github.positionpal"

    with(rootProject.libs.plugins) {
        apply(plugin = "java-library")
        apply(plugin = "scala")
        apply(plugin = cucumber.jvm.get().pluginId)
        apply(plugin = scala.extras.get().pluginId)
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
        dotenvConfig.environmentVariables().forEach { environment(it.key, it.value) }
    }

    cucumber {
        val featureFilesDirectory = "features"
        featurePath = "src/test/resources/$featureFilesDirectory"
        glue = "$group.location.${this@allprojects.name}.$featureFilesDirectory"
        main = "io.cucumber.core.cli.Main"
        plugin = arrayOf("pretty")
    }

    tasks.withType<Test> {
        if (projectDir.resolve(cucumber.featurePath).exists()) {
            dependsOn("cucumber")
        }
        dotenvConfig.environmentVariables().forEach { environment(it.key, it.value) }
    }
}
