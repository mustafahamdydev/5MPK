import org.jetbrains.kotlin.konan.properties.Properties
import java.io.InputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
    id("com.chaquo.python")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.wanderer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wanderer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

chaquopy{
    defaultConfig{
        version = "3.8" //Python version 3.8 is max
        pyc {
            src = false
        }
        pip{ //Python libraries used in the python code file
            install("pandas")
            install("networkx")
            install("geopy")
            install("shapely")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    implementation (libs.google.maps.services)
    implementation (libs.slf4j.simple)
    implementation(libs.material)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.places)
    implementation (libs.easypermissions)
    implementation (libs.circleimageview)

}