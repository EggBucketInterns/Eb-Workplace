plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.EBworkplace.scannerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.EBworkplace.scannerapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_14
        targetCompatibility = JavaVersion.VERSION_14
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation(libs.com.google.firebase.firebase.appcheck.playintegrity)
    implementation(libs.google.firebase.storage)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.base)
    implementation(libs.play.services.mlkit.document.scanner)
    implementation("com.google.android.gms:play-services-auth:20.3.0")
    implementation("androidx.browser:browser:1.8.0")
}