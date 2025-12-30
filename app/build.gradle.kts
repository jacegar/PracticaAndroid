plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.practicaandroid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.practicaandroid"
        minSdk = 26

        //Error informativo, no pasa na
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room - SQLite Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    //Barra navegacion
    implementation(libs.material)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}