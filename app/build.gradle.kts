plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Firebase Plugin
}

android {
    namespace = "com.example.pulseguard"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pulseguard"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true // Enables View Binding
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
        sourceCompatibility = JavaVersion.VERSION_17 // Updated to Java 17 for better performance
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Import Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))

    // Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore") // Optional
    implementation("com.google.firebase:firebase-storage") // Optional

    // Google Sign-In (Use the latest version)
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Google Fit API for Health Data (Updated to 21.1.0)
    implementation("com.google.android.gms:play-services-fitness:21.1.0")

    // AndroidX & Material Design
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Testing Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
