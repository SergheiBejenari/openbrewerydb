plugins {
    java
    id("io.qameta.allure") version "2.11.2"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.testng:testng:7.10.2")
    testImplementation(platform("io.rest-assured:rest-assured-bom:5.5.6"))
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
    testImplementation(platform("org.slf4j:slf4j-bom:2.0.13"))
    testImplementation("ch.qos.logback:logback-classic:1.4.14")
    testImplementation("io.qameta.allure:allure-testng:2.29.0")
    testImplementation("io.qameta.allure:allure-rest-assured:2.29.0")
    testRuntimeOnly("org.aspectj:aspectjweaver:1.9.22.1")
}

configurations.all {
    exclude(group = "org.slf4j", module = "slf4j-simple")
}

tasks.test {
    useTestNG {
        suites("src/test/resources/testng.xml")
    }
}

allure {
    adapter {
        aspectjWeaver.set(true)
        frameworks {
            testng {
                adapterVersion.set("2.29.0")
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}