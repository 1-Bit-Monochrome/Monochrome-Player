plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.monochrome.monochrome_player"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.monochrome.monochrome_player"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.media:media:1.6.0")
    // RecyclerView is used to display the list of songs on the home screen
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("ir.mahozad.multiplatform:wavy-slider:2.2.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
