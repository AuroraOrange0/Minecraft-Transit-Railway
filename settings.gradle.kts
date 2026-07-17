pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://maven.kikugie.dev/releases")
		maven("https://maven.kikugie.dev/snapshots")
		maven("https://maven.fabricmc.net/")
		maven("https://maven.neoforged.net/releases/")
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.9.6"
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
	create(rootProject) {
		mapBuilds { _, node ->
			"build-${node.project.substringAfter('-')}.gradle.kts"
		}

		versions(
			"1.21.11-fabric" to "1.21.11",
		)

		vcsVersion = "1.21.11-fabric"
	}
}

rootProject.name = "Minecraft-Transit-Railway"
