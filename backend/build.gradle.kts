plugins {
    java
    id("org.springframework.boot") version "4.1.1"
}

val postgisImage = "postgis/postgis:16-3.5@sha256:94146ac37bc61e2322f88016056c5920729cb8c64c8542ed590af8fc2abdac07"
val openApiGenerator = configurations.create("openApiGenerator")

group = "com.cheffybites"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations.configureEach {
    resolutionStrategy.activateDependencyLocking()
}

repositories {
    mavenCentral()
}

// Create integrationTest source set and extend its generated configurations
val integrationTestSourceSet = sourceSets.create("integrationTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
    compileClasspath += sourceSets.test.get().output
    runtimeClasspath += sourceSets.test.get().output
}

configurations.named(integrationTestSourceSet.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}

configurations.named(integrationTestSourceSet.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-micrometer-tracing-opentelemetry")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("com.github.f4b6a3:uuid-creator:6.1.1")

    // Add integration test dependencies using the source set configuration names
    add(integrationTestSourceSet.implementationConfigurationName, "org.springframework.boot:spring-boot-starter-test")
    add(integrationTestSourceSet.implementationConfigurationName, "org.springframework.boot:spring-boot-starter-webmvc-test")
    add(integrationTestSourceSet.implementationConfigurationName, "org.springframework.boot:spring-boot-starter-security-test")
    add(integrationTestSourceSet.implementationConfigurationName, platform("org.testcontainers:testcontainers-bom:2.0.5"))
    add(integrationTestSourceSet.implementationConfigurationName, "org.testcontainers:testcontainers-postgresql")
    add(integrationTestSourceSet.implementationConfigurationName, "org.testcontainers:testcontainers-junit-jupiter")
    add(integrationTestSourceSet.implementationConfigurationName, "com.github.f4b6a3:uuid-creator:6.1.1")
    add(integrationTestSourceSet.implementationConfigurationName, "org.assertj:assertj-core:3.27.3")

    openApiGenerator("org.openapitools:openapi-generator-cli:7.25.0")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.test {
    description = "Runs fast unit and Spring shell tests."
    exclude("**/*IT.class")
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs disposable Testcontainers compatibility tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    include("**/*IT.class")
    shouldRunAfter(tasks.test)
    systemProperty("cheffy.test.postgis-image", postgisImage)
}

tasks.check {
    dependsOn(integrationTest)
}
val testRuntimeClasspathFiles =
    objects.fileCollection().from(configurations.named("testRuntimeClasspath"))

tasks.register("verifyResolvedJUnit6") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Fails unless every resolved JUnit Jupiter and Platform artifact is major 6."

    inputs.files(testRuntimeClasspathFiles)

doLast {
    val junitArtifacts = inputs.files.files
        .filter {
            it.name.startsWith("junit-jupiter-") ||
                it.name.startsWith("junit-platform-")
        }

    check(junitArtifacts.isNotEmpty()) {
        "No JUnit Jupiter or Platform artifacts resolved."
    }

    val versionPattern = Regex("""-(\d+)\.\d+.*\.jar$""")

    val invalid = junitArtifacts.filter { file ->
        val match = versionPattern.find(file.name)
        match == null || match.groupValues[1] != "6"
    }

    check(invalid.isEmpty()) {
        "Non-JUnit-6 artifacts resolved: ${invalid.map { it.name }}"
    }
}
}

tasks.check {
    dependsOn("verifyResolvedJUnit6")
}

tasks.register<JavaExec>("generateApiClient") {
    group = "openapi"
    description = "Generates the deterministic TypeScript fetch client into -PapiClientOutput."
    classpath = openApiGenerator
    mainClass = "org.openapitools.codegen.OpenAPIGenerator"

    val outputDirectory = providers.gradleProperty("apiClientOutput")
        .orElse(layout.projectDirectory.dir("../packages/api-client/src/generated").asFile.absolutePath)

    args(
        "generate",
        "-i", layout.projectDirectory.dir("../packages/api-client/openapi/cheffy-bites-v1.yaml").asFile.absolutePath,
        "-g", "typescript-fetch",
        "-o", outputDirectory.get(),
        "--global-property", "apiDocs=false,modelDocs=false,apiTests=false,modelTests=false",
        "--additional-properties", "supportsES6=true,typescriptThreePlus=true,withInterfaces=true,useSingleRequestParameter=true"
    )
}

dependencyLocking {
    lockAllConfigurations()
}
