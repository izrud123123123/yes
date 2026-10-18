# Aurora Visuals (Fabric, Minecraft 1.21.11, client-side)

## Get the jar (no local setup)
1. Create a GitHub repo and push this folder to it.
2. Open the repo's "Actions" tab -> "Build jar" -> wait for the green check.
3. Download the "aurora-visuals-jar" artifact. Unzip it: that's your mod jar.

## Or build locally (Java 21)
Install Gradle 9.3+ (or generate a wrapper from fabricmc.net/develop), then:
    gradle build
The jar is in build/libs/aurora-visuals-1.1.0.jar

## Install
Fabric Loader 0.18.1+ for 1.21.11, Fabric API for 1.21.11, then put the jar in .minecraft/mods.

## Use
Right Shift in a world opens the settings (rebindable). Config: .minecraft/config/aurora_visuals.json
