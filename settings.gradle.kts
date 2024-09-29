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
include(":zebra:core:clipboard")
include(":zebra:core:biometry")
include(":zebra:core:datastore")
include(":zebra:core:peer")
include(":zebra:feature:share")
include(":zebra:core:network")
include(":zebra:feature:feature-flag")
