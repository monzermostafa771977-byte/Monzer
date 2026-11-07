import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// قراءة بيانات المفتاح من keystore.properties (محلياً) أو من متغيرات البيئة (في GitHub Actions)
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(name: String, envName: String): String? =
    keystoreProps.getProperty(name) ?: System.getenv(envName)

val ksFile = secret("storeFile", "HARES_KEYSTORE_FILE")
val ksPassword = secret("storePassword", "HARES_KEYSTORE_PASSWORD")
val ksAlias = secret("keyAlias", "HARES_KEY_ALIAS")
val ksKeyPassword = secret("keyPassword", "HARES_KEY_PASSWORD")
val hasReleaseKey = listOf(ksFile, ksPassword, ksAlias, ksKeyPassword).all { !it.isNullOrBlank() } &&
    file(ksFile!!).exists()

android {
    namespace = "com.monzer.hares"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.monzer.hares"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.3-alpha"
    }

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(ksFile!!)
                storePassword = ksPassword
                keyAlias = ksAlias
                keyPassword = ksKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // بدون مفتاح release يُوقَّع بمفتاح debug كي يمكن تثبيته للتجربة فقط
            signingConfig = if (hasReleaseKey) signingConfigs.getByName("release")
                            else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // لا نترك فحص Lint يوقف بناء النسخة التجريبية
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// اسم ملف ثابت وواضح عند المشاركة: Hares-0.3-alpha-release.apk
base.archivesName.set("Hares-0.3-alpha")
