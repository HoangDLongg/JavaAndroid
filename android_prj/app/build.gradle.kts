plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.hoanglong171.movies"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hoanglong171.movies"
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
    buildFeatures {
        viewBinding = true
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

    // ViewPager2 và các thư viện giao diện
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0") // Thêm CardView cho item_movie.xml

    // Cập nhật Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")

    // Cập nhật ChipNavigationBar
    implementation("com.github.ismaeldivita:chip-navigation-bar:1.3.4")

    // BlurView
    implementation("com.github.Dimezis:BlurView:version-2.0.3")
    implementation ("com.google.android.exoplayer:exoplayer:2.19.1")
    // Firebase (sử dụng BOM để quản lý phiên bản)
    implementation(platform("com.google.firebase:firebase-bom:33.2.0")) // Dùng phiên bản ổn định
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth") // Thêm Firebase Auth
    implementation("com.google.firebase:firebase-database")
    implementation("com.firebaseui:firebase-ui-auth:8.0.2") // Phiên bản mới hơn, tương thích với BOM
    implementation ("com.google.firebase:firebase-storage:21.0.0")
    // Thêm annotation để hỗ trợ NonNull
    implementation("androidx.annotation:annotation:1.7.0")
}