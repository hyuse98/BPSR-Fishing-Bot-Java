import org.gradle.internal.os.OperatingSystem

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

val defaultOs = when {
    OperatingSystem.current().isWindows -> "windows-x86_64"
    OperatingSystem.current().isLinux -> "linux-x86_64"
    else -> "windows-x86_64"
}

val targetOs = project.findProperty("targetOs") as? String ?: defaultOs

group = "org.hyuse98"
version = "Java-V2-$targetOs"

repositories {
    mavenCentral()
}

val javacvVersion = "1.5.10"
val opencvVersion = "4.9.0-$javacvVersion"
val openblasVersion = "0.3.26-$javacvVersion"

dependencies {
    implementation("org.bytedeco:javacv:$javacvVersion")

    implementation("org.bytedeco:opencv:$opencvVersion")
    implementation("org.bytedeco:opencv:$opencvVersion:$targetOs")

    implementation("org.bytedeco:openblas:$openblasVersion")
    implementation("org.bytedeco:openblas:$openblasVersion:$targetOs")

    implementation("net.java.dev.jna:jna-platform:5.13.0")

    implementation("com.formdev:flatlaf:3.2.5")

    implementation("com.google.code.gson:gson:2.11.0")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "fishbot.Main"
    }
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.register<GradleBuild>("buildWindows") {
    group = "BPSR Build"
    description = "Build Windows .jar"

    tasks = listOf("shadowJar")
    startParameter.projectProperties = mapOf("targetOs" to "windows-x86_64")
}

tasks.register<GradleBuild>("buildLinux") {
    group = "BPSR Build"
    description = "Build Linux .jar"

    tasks = listOf("shadowJar")
    startParameter.projectProperties = mapOf("targetOs" to "linux-x86_64")
}

tasks.register("buildAll") {
    group = "BPSR Build"
    description = "Run all build tasks: [Windows, Linux]"

    dependsOn("buildWindows", "buildLinux")
}

tasks.test {
    useJUnitPlatform()
}