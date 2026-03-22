pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "Forge"
			url = uri("https://maven.minecraftforge.net/")
		}
		maven {
			name = "Parchment"
			url = uri("https://maven.parchmentmc.org")
		}
        google()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "TouchController"

include("mod:resources")
include("mod:common")
include("mod:common-fabric")
include("mod:common-lwjgl3")

// Legacy versions - disabled for testing
//include("mod:1.12.2:forge-1.12.2")
//include("mod:1.16.5:fabric-1.16.5")
//include("mod:1.16.5:forge-1.16.5")

//include("mod:common-1.20.x")
//include("mod:1.20.1:common-1.20.1")
//include("mod:1.20.1:fabric-1.20.1")
//include("mod:1.20.1:forge-1.20.1")
// Disabled: 1.20.4, 1.20.6
//include("mod:1.20.4:common-1.20.4")
//include("mod:1.20.4:fabric-1.20.4")
//include("mod:1.20.4:forge-1.20.4")
//include("mod:1.20.4:neoforge-1.20.4")
//include("mod:1.20.6:common-1.20.6")
//include("mod:1.20.6:fabric-1.20.6")
//include("mod:1.20.6:forge-1.20.6")
//include("mod:1.20.6:neoforge-1.20.6")

include("mod:common-1.21.x")
include("mod:common-1.21-1.21.4")
include("mod:common-1.21-1.21.5")

include("mod:common-1.21-1.21.1")
// Disabled for testing - only 1.21.1 enabled
//include("mod:1.21:common-1.21")
//include("mod:1.21:fabric-1.21")
//include("mod:1.21:forge-1.21")
//include("mod:1.21:neoforge-1.21")
include("mod:1.21.1:common-1.21.1")
include("mod:1.21.1:fabric-1.21.1")
include("mod:1.21.1:forge-1.21.1")
include("mod:1.21.1:neoforge-1.21.1")

// Disabled: 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7
//include("mod:common-1.21.3-1.21.4")
//include("mod:1.21.3:common-1.21.3")
//include("mod:1.21.3:fabric-1.21.3")
//include("mod:1.21.3:forge-1.21.3")
//include("mod:1.21.3:neoforge-1.21.3")
//include("mod:1.21.4:common-1.21.4")
//include("mod:1.21.4:fabric-1.21.4")
//include("mod:1.21.4:forge-1.21.4")
//include("mod:1.21.4:neoforge-1.21.4")

//include("mod:common-1.21.3-1.21.5")
//include("mod:1.21.5:common-1.21.5")
//include("mod:1.21.5:fabric-1.21.5")
//include("mod:1.21.5:forge-1.21.5")
//include("mod:1.21.5:neoforge-1.21.5")

//include("mod:common-1.21.6-1.21.8")
//include("mod:1.21.6:common-1.21.6")
//include("mod:1.21.6:fabric-1.21.6")
//include("mod:1.21.6:forge-1.21.6")
//include("mod:1.21.6:neoforge-1.21.6")
//include("mod:1.21.7:common-1.21.7")
//include("mod:1.21.7:fabric-1.21.7")
//include("mod:1.21.7:forge-1.21.7")
//include("mod:1.21.7:neoforge-1.21.7")
//include("mod:1.21.8:common-1.21.8")
//include("mod:1.21.8:fabric-1.21.8")
//include("mod:1.21.8:forge-1.21.8")
//include("mod:1.21.8:neoforge-1.21.8")

include("log4j-slf4j2-impl")
include("proxy-server")
include("proxy-client")
include("proxy-windows")
include("proxy-linux")
include("proxy-client-android")
include("proxy-server-android")
include("combine")
include("combine-ui")
include("common-data")
