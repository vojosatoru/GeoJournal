// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Plugin KSP (Wajib untuk Database Room)
    // Gunakan 2.0.20-1.0.24 jika pakai Kotlin terbaru, atau 2.0.0-1.0.21 untuk yang lebih lama.
    id("com.google.devtools.ksp") version "2.0.20-1.0.24" apply false
}