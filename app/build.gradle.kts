plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tusomeapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tusomeapp"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
dependencies {
    implementation("de.hdodenhof:circleimageview:3.1.0")
}
dependencies {
    implementation("com.google.android.material:material:1.9.0")  // Use latest version
}
dependencies {
    implementation("androidx.compose.material:material-icons-extended:1.5.4") // Or latest version
}
