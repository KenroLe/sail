package art.xeloboyo.airshipsloader.gameprovider.services;

import art.xeloboyo.airshipsloader.gameprovider.patch.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.entrypoint.EntrypointUtils;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import org.objectweb.asm.tree.ClassNode;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is based on <a href="https://github.com/FabricMC/fabric-loader/blob/6adfe08efeb04c8dde829053e3cc546c01ef8415/minecraft/src/main/java/net/fabricmc/loader/impl/game/minecraft/Hooks.java">.../fabricmc/loader/impl/game/minecraft/Hooks.java</a>
 * <br>
 * These static methods are called by ASM injections done by the (this) loader.
 */
public final class AirshipGameHooks{
    public static final String INTERNAL_NAME = AirshipGameHooks.class.getName().replace('.', '/');

    /**
     * This method is called by code injected in {@link AirshipsEntrypointPatch} #injectClientHook(ClassNode)
     */
    public static void initClient() {
        Path runDir = Paths.get(".");
        FabricLoaderImpl.INSTANCE.prepareModInit(runDir, FabricLoaderImpl.INSTANCE.getGameInstance());
        // Call the entrypoints of all mods specified in fabric.mods.json > entrypoints > main
        EntrypointUtils.invoke("main", ModInitializer.class, ModInitializer::onInitialize);
        // Call the entrypoints of all mods specified in fabric.mods.json > entrypoints > client
        EntrypointUtils.invoke("client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);
    }

    /**
     * This method is called by code injected in {@link AirshipsEntrypointPatch} #injectServerHook(ClassNode)
     */
    public static void initServer() {
        Path runDir = Paths.get(".");
        FabricLoaderImpl.INSTANCE.prepareModInit(runDir, FabricLoaderImpl.INSTANCE.getGameInstance());
        EntrypointUtils.invoke("main", ModInitializer.class, ModInitializer::onInitialize);
        EntrypointUtils.invoke("server", DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeServer);
    }

    public static String getBrand(){
        return "Modded (fabric " + FabricLoaderImpl.VERSION + ")";
    }

    public static void insertBranding(final StringBuilder brand) {
        brand.append(" ");
        brand.append(getBrand());
    }

    public static String insertBranding(final String brand) {
        String fabricBrand = getBrand();
        return brand + "\n" + fabricBrand;
    }
}
