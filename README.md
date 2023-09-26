# Airships Fabric Modloader

This is a modloader based on [Mindustry Game Provider](https://github.com/Qendolin/mindustry-fabric-loader)
to load fabric mods for [Airships Conquer the skies](https://store.steampowered.com/app/342560/Airships_Conquer_the_Skies/)

# How Fabric Loader works
Fabric[^fabric] uses Java bytecode manipulation powered by ASM[^asm] 
to inject the loader and mod's mixin[^mixin] code into the game classes as they are loaded.

To do this, fabric has to run code before the game is even loaded. 
In fact, when launching a game with fabric, the entry class is not one of the game but KnotClient[^2].

Knot[^knot] is fabric's game launcher.
It is responsible for setting up the injection and calling your game provider
before jumping to the game's entry point (_e.g.: `main` function_).

The game provider's job is to provide information to fabric and inject initialization hooks
that can be used by mods.

For example [ModInitializer#onInitialize](https://github.com/FabricMC/fabric-example-mod/blob/242f56a70245d99e7463d3f76f4af8038398bd5a/src/main/java/net/fabricmc/example/ExampleMod.java#L14)
is made possible by hook that is provided by the game provider.

# Files and Folders

- `art/xeloboyo/airshipsloader/gameprovider/services`
  This package contains
- `art/xeloboyo/airshipsloader/gameprovider/patch`
  This package contains AMS[^asm] bytecode patches
  that are always applied by the loaded.  
  The `AirshipsEntrypointPatch` injects the game initialization hooks.  
  The `AirshipsBrandingPatch` injects some code to show "Modded Fabric" on the main menu.
- `META-INF/services/net.fabricmc.loader.impl.game.GameProvider`  
  This file is used by Knot[^knot] to determine the GameProvider class[^1].  
  It has to contain exactly one line with the fully-qualified class name.

### Footnotes

[^fabric]: Fabric is a lightweight, experimental modding toolchain for Minecraft. [fabricmc.net](https://fabricmc.net/)  
[^knot]: The Fabric game launcher  
[^asm]: ASM is an all purpose Java bytecode manipulation and analysis framework. [asm.ow2.io](https://asm.ow2.io/)  
[^mixin]: Mixin is a trait/mixin and bytecode weaving framework for Java using ASM. [GitHub](https://github.com/SpongePowered/Mixin)  
[^1]: [Knot.java#L173](https://github.com/FabricMC/fabric-loader/blob/6adfe08efeb04c8dde829053e3cc546c01ef8415/src/main/java/net/fabricmc/loader/impl/launch/knot/Knot.java#L173)  
[^2]: [KnotClient.java](https://github.com/FabricMC/fabric-loader/blob/6adfe08efeb04c8dde829053e3cc546c01ef8415/src/main/java/net/fabricmc/loader/impl/launch/knot/KnotClient.java)  