plugins {
    java
    id("org.springframework.boot") version "4.0.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openrewrite.rewrite") version "7.39.0"
}

group = "dev.gaphunter"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-resttestclient")
    testImplementation("org.springframework.boot:spring-boot-restclient")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    rewrite(platform("org.openrewrite.recipe:rewrite-recipe-bom:3.37.0"))
    rewrite("org.openrewrite.recipe:rewrite-spring")
}

rewrite {
    activeRecipe("org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0")
}

tasks.test {
    useJUnitPlatform()
}
