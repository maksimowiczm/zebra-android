pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Zebra"
include(":app")
include(":zebra:core:common-ui")
include(":zebra:core:data")
include(":zebra:feature:vault")
include(":zebra:core:database")
include(":zebra:core:domain")
include(":zebra:core:common")
