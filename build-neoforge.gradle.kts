import org.apache.tools.ant.filters.ReplaceTokens
import org.mtr.BuildTools
import org.mtr.core.Generator
import org.mtr.core.WebserverSetup

plugins {
	id("net.neoforged.moddev")
	id("dev.kikugie.fletching-table.neoforge") version "+"
}

base.archivesName = property("mod.id") as String
version = "${property("mod.version")}+${sc.current.version}-neoforge"

repositories {
	mavenCentral()
	maven { url = uri("https://repo.codemc.org/repository/maven-public") } // Occlusion Culling
	maven { url = uri("https://repo.essential.gg/repository/maven-public") } // Elementa and UniversalCraft
	maven {
		url = uri("https://maven.pkg.github.com/Minecraft-Transit-Railway/Transport-Simulation-Core")
		credentials {
			username = providers.gradleProperty("gpr.user").getOrNull() ?: "github-actions"
			password = providers.gradleProperty("gpr.key").getOrNull() ?: System.getenv("GITHUB_TOKEN")
		}
	}
}

val buildTools = BuildTools(sc.current.version, "neoforge", project.property("mod.version").toString(), project.rootDir)
val requiredJava = when {
	sc.current.parsed < "26.0" -> JavaVersion.VERSION_21
	else -> JavaVersion.VERSION_26
}

java {
	withSourcesJar()
	targetCompatibility = requiredJava
	sourceCompatibility = requiredJava
}

dependencies {
	implementation("org.mtr:transport-simulation-core:+")
	implementation("com.logisticscraft:occlusionculling:+")
	implementation("gg.essential:elementa:${property("dependency.elementa")}")
	implementation("gg.essential:universalcraft-${property("dependency.universal_craft_minecraft")}-neoforge:${property("dependency.universal_craft")}")
	implementation("org.jspecify:jspecify:+")
}

tasks {
	processResources {
		val properties = mapOf(
			"mod_id" to project.property("mod.id"),
			"mod_name" to project.property("mod.name"),
			"mod_description" to project.property("mod.description"),
			"mod_license" to project.property("mod.license"),
			"mod_author" to project.property("mod.author"),
			"mod_version" to project.property("mod.version"),
			"minecraft_version" to sc.current.version,
		)

		filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
			expand(properties)
		}

		exclude("**/fabric.mod.json")
	}

	javadoc {
		// Suppress "missing" doclint only (generated classes don't need javadoc)
		(options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all,-missing", "-quiet")
	}

	withType<JavaCompile>().configureEach {
		options.compilerArgs.addAll(
			listOf(
				"-Xlint:all",
				"-Xlint:-serial",     // No Java serialization
				"-Xlint:-processing", // Lombok annotation processor noise
				"-Xlint:-this-escape" // Safe: schema constructor pattern
			)
		)
	}

	register<Copy>("buildAndCollect") {
		description = "Builds the mod and collects the JAR and sources JAR into the build/libs directory with versioned naming."
		group = "build"
		from(jar.map { it.archiveFile })
		into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
		dependsOn("build")
	}

	register("setupWebsiteFiles") {
		description = "Generates TypeScript files for the website based on the resource schema."
		Generator.generateTypeScript(project, "schema/resource", "../../website/src/app/entity/generated")
	}

	register("setupFiles") {
		description = "Sets up necessary files for the mod, including generating Java classes from templates and processing translations."

		copy {
			outputs.upToDateWhen { false }
			from("../../src/main/KeysTemplate.java")
			into("../../src/main/java/org/mtr")
			filter<ReplaceTokens>(mapOf("tokens" to mapOf("version" to "${project.property("mod.version")}+${sc.current.version}", "debug" to "${project.property("debug")}")))
			rename("(.+)Template.java", "$1.java")
		}

		buildTools.downloadTranslations(project.property("key.crowdin").toString())
		buildTools.generateTranslations()
		buildTools.copyVehicleTemplates()
		buildTools.getPatreonList(project.property("key.patreon").toString())
		buildTools.setupObjLibrary()
		Generator.generateJava(project, "schema/config", "generated/config", false, "config")
		Generator.generateJava(project, "schema/resource", "generated/resource", false, "core.data", "resource")
		Generator.generateJava(project, "schema/legacy", "legacy/generated/resource", false)
		WebserverSetup.setup(project.rootDir, "", "")
		buildTools.fixImports(project, "generated/config")
		buildTools.fixImports(project, "generated/resource")
		buildTools.fixImports(project, "legacy/generated/resource")
	}
}
