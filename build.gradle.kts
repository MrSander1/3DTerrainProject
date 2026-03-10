plugins {
    id("java")
}

// 1. Define versions (replacing the Maven ${version} variables)
val lwjglVersion = "3.4.1" // or your preferred version
val tinylogVersion = "2.7.0"
val jomlVersion = "1.10.8"
val imguiVersion = "1.90.0"
val nativeTarget = "windows"


// 2. Logic to handle the ${native.target} classifier
val lwjglNatives = when (System.getProperty("os.name").lowercase()) {
    "mac os x" -> if (System.getProperty("os.arch").startsWith("aarch64")) "natives-macos-arm64" else "natives-macos"
    "linux" -> "natives-linux"
    else -> "natives-windows"
}

repositories {
    mavenCentral()
}

dependencies {
    // Tinylog
    implementation("org.tinylog:tinylog-api:$tinylogVersion")
    implementation("org.tinylog:tinylog-impl:$tinylogVersion")

    // LWJGL Core
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$lwjglNatives")

    // GLFW (Window management)
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:$lwjglNatives")

    // OpenGL (Graphics)
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:$lwjglNatives")

    implementation("org.lwjgl:lwjgl-stb:${lwjglVersion}")
    runtimeOnly("org.lwjgl:lwjgl-stb:$lwjglVersion:natives-windows")

    implementation("org.joml:joml:${jomlVersion}")

    implementation("io.github.spair:imgui-java-binding:$imguiVersion")
    runtimeOnly("io.github.spair:imgui-java-natives-windows:$imguiVersion")
}