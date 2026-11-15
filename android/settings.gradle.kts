// Onde o Gradle busca os PLUGINS (Android Gradle Plugin, Kotlin)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Onde o Gradle busca as BIBLIOTECAS (AndroidX, Material, Retrofit...)
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SoundMatch"
include(":app")
