plugins {
    id("com.android.application")
}

android {
    namespace = "de.hsos.habiton"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.hsos.habiton"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation ("it.xabaras.android:recyclerview-swipedecorator:1.4")
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation ("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.databinding:databinding-runtime:8.2.2")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
}