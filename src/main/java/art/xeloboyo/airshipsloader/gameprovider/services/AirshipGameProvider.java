package art.xeloboyo.airshipsloader.gameprovider.services;

import art.xeloboyo.airshipsloader.gameprovider.*;
import art.xeloboyo.airshipsloader.gameprovider.GameVersion.*;
import art.xeloboyo.airshipsloader.gameprovider.patch.AirshipsBrandingPatch;
import art.xeloboyo.airshipsloader.gameprovider.patch.AirshipsEntrypointPatch;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.game.*;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.fabricmc.loader.impl.metadata.ContactInformationImpl;
import net.fabricmc.loader.impl.util.Arguments;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The information in this class is based on my research into fabric-loader's code.
 * Non of this is official and might be incorrect.
 */
public class AirshipGameProvider implements GameProvider {

    /**
     * The fully-qualified class name of the entrypoint client class.
     * Entrypoint in this context meaning the class where {@link AirshipsEntrypointPatch} injects its hooks
     * <br>
     * Fou you this might be the same as {@link #CLIENT_MAIN}.
     **/
    public static final String CLIENT_ENTRYPOINT = "com.zarkonnen.airships.AirshipGame";
    /** The fully-qualified class name of the main client class */
    public static final String CLIENT_MAIN = "com.zarkonnen.airships.Main";
    /**
     * The fully-qualified class name of the entrypoint client class.
     * Entrypoint in this context meaning the class where {@link AirshipsEntrypointPatch} injects its hooks
     * <br>
     * Fou you this might be the same as {@link #SERVER_MAIN}.
     **/
    public static final String SERVER_ENTRYPOINT =  "com.zarkonnen.airships.Server";
    /** The fully-qualified class name of the main server class */
    public static final String SERVER_MAIN =  "com.zarkonnen.airships.Server";
    /** All possible entrypoints */
    private static final String[] ENTRYPOINTS = new String[]{
            CLIENT_ENTRYPOINT, SERVER_ENTRYPOINT
    };
    /**
     * If your game has arguments with sensitive data you should add them here,
     * but I am not sure why.
     */
    private static final Set<String> SENSITIVE_ARGS = new HashSet<>(Arrays.asList(
            // all lowercase without --
            "savedir",
            "secrets"));
    /**
     * The transformer will orchestrate the game patching.
     */
    private static final GameTransformer TRANSFORMER = new GameTransformer(
            new AirshipsEntrypointPatch(),
            new AirshipsBrandingPatch());

    /** The parsed command line arguments */
    private Arguments arguments;
    /**
     * The fully-qualified class name of the entrypoint that is currently used.
     * It is either {@link #CLIENT_ENTRYPOINT} or {@link #SERVER_ENTRYPOINT} depending on the launch settings.
     **/
    private String entrypoint;
    /** The path of the game jar that is currently used */
    private Path gameJar;
    private GameVersion gameVersion;
    private Collection<Path> validParentClassPath;

    /** Just a simple getter to allow mods to access the detailed version info */
    public GameVersion getGameVersion() {
        return gameVersion;
    }

    /**
     * Like {@link #getGameName} this has no specific use outside of logging.
     * <br>
     * The returned string does not have to follow any specific format.
     */
    @Override
    public String getRawGameVersion() {
        if (gameVersion == null) return "0.0.0";
        return gameVersion.toString();
    }

    /**
     * This list should at least contain the base game (as a 'pseudo' mod).
     * But can additionally contain any number of mods that should always be loaded (e.g. a core modding api),
     * although I would not recommend it (even fabric-api is an optional mod in minecraft).
     * <br>
     * Adding the game as a 'pseudo' mod is very useful for other mods, as they can f.e. use it to declare (in-)compatibilities.
     * <br>
     * The metadata can contain the same info as <a href="https://fabricmc.net/wiki/documentation:fabric_mod_json">fabric.mod.json</a> would usually contain.
     * Check the link to find out which map keys are allowed.
     */
    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        HashMap<String, String> contactInfo = new HashMap<>();
        contactInfo.put("homepage", "http://www.zarkonnen.com/");

        BuiltinModMetadata.Builder metaData =
                new BuiltinModMetadata.Builder(getGameId(), getNormalizedGameVersion())
                        .setName(getGameName())
                        .addAuthor("Zarkonnen", contactInfo)
                        .setContact(new ContactInformationImpl(contactInfo))
                        .setDescription("The base Airships game");

        return Collections.singletonList(new BuiltinMod(Collections.singletonList(gameJar), metaData.build()));
    }

    /**
     * The game id should be lowercase and contain no spaces. (As per the rule I just made up)
     * <br>
     * It is used, amongst other things, in obfuscation mappings.
     * <br>
     * Choose wisely as this should never change.
     */
    @Override
    public String getGameId() {
        return "airships-base";
    }

    /**
     * This version is (can be) used as part of the game id,
     * as such it should contain no spaces and only ascii characters.
     * <br>
     * The returned string should follow a consistent (normalized) format.
     * The specific format is not important, as long as is clearly defined for all game versions.
     */
    @Override
    public String getNormalizedGameVersion() {
        if (gameVersion == null) return "0.0.0";
        return gameVersion.toStringVersion().getFriendlyString();
    }

    /**
     * The game name is used in logging output but has no specific use case.
     * <br>
     * Spaces should be fine but non-ascii characters should be avoided.
     */
    @Override
    public String getGameName() {
        return "Airships Conquer the Skies";
    }

    /**
     * TODO: Is used to inject hooks and ?
     * <br><br>
     * This method only is called after {@link #initialize(FabricLauncher)}
     */
    @Override
    public String getEntrypoint() {
        return entrypoint;
    }

    /**
     * The root directory of the game.
     * Within this directory the {@code mods} and {@code config}, among others, directories are created.
     * As such the directory <b>must</b> be writeable.
     * <br><br>
     * This method only is called after {@link #initialize(FabricLauncher)}
     */
    @Override
    public Path getLaunchDirectory() {
        if (arguments == null) {
            // Minecraft's game provider just returns Paths.get(".")
            // but I feel like an exception is the proper response.
            throw new IllegalStateException("invoked too early?");
        }

        return Paths.get(arguments.getOrDefault("gameDir", "."));
    }

    /**
     * Isn't used in fabric-loader, but should return true if the game jar is obfuscated.
     * <br>
     * Note: Deobfuscation does not happen automatically, see <a href="https://github.com/FabricMC/fabric-loader/blob/6adfe08efeb04c8dde829053e3cc546c01ef8415/minecraft/src/main/java/net/fabricmc/loader/impl/game/minecraft/MinecraftGameProvider.java#L290">MinecraftGameProvider.java#L290</a>
     */
    @Override
    public boolean isObfuscated() {
        return false;
    }

    /**
     * I don't know, but should probably be false in 99.99%.
     */
    @Override
    public boolean requiresUrlClassLoader() {
        return false;
    }

    /**
     * Disabled game providers are not used.
     * I can't see a reason why this should ever return false.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * This method should find the game jar and extract information (e.g.: version) from the game files.
     * It should also parse and store the command line args.
     * <br>
     * Fabric defines three system properties to specify the game jar path explicitly.
     * See <a href="https://github.com/FabricMC/fabric-loader/blob/6adfe08efeb04c8dde829053e3cc546c01ef8415/src/main/java/net/fabricmc/loader/impl/util/SystemProperties.java#L26-L28">SystemProperties.java#L26-L28</a>
     * These properties should not be ignored.
     * <br><br>
     * This method is called just after the game provider is instantiated.
     * @param launcher
     * @param args The command line args given to Knot
     * @return {@code true} if the game was found
     */
    @Override
    public boolean locateGame(FabricLauncher launcher, String[] args) {
        // I'm not very sure about the implementation of this method
        // Minecraft's is quite complicated and all other examples aren't very good
        var envType = launcher.getEnvironmentType();

        arguments = new Arguments();
        arguments.parse(args);

        List<String> gameLocations = new ArrayList<>();
        // Add all usual jar locations
        gameLocations.add("C:/Program Files (x86)/Steam/steamapps/common/Airships Conquer the Skies/Airships.jar");
        gameLocations.add("./Airships.jar");

        List<Path> jarPaths = gameLocations.stream()
        .map(path -> Paths.get(path).toAbsolutePath().normalize())
        .filter(Files::exists).toList();

        // Finds the first jar that contains any class specified by the last parameter
        GameProviderHelper.FindResult result = GameProviderHelper.findFirst(jarPaths, new HashMap<>(), true, ENTRYPOINTS);

        if(result == null || result.path == null){
            Log.error(LogCategory.GAME_PROVIDER, "Could not locate game. Looked at: \n" + gameLocations.stream()
            .map(path -> " - " + Paths.get(path).toAbsolutePath().normalize())
            .collect(Collectors.joining("\n")));
            return false;
        }

        entrypoint = result.name;
        gameJar = result.path;
        gameVersion = new Builder().setNumber(1).setBuild(1).setRevision(1).build();

        try{
            var classifier = new LibClassifier<>(AirshipsLibraries.class, envType, this);

            classifier.process(gameJar);
            classifier.process(launcher.getClassPath());


            validParentClassPath = classifier.getSystemLibraries();
        }catch(Exception e){
            Log.info(LogCategory.GAME_PROVIDER,"Something died when locating game:" + e.toString());
        }
        processArgumentMap(arguments);
        return true;
    }

    /**
     * Command line argument parsing
     */
    private void processArgumentMap(Arguments arguments) {
        if (!arguments.containsKey("gameDir")) {
            arguments.put("gameDir", Paths.get(".").toAbsolutePath().normalize().toString());
        }

        Path launchDir = Path.of(arguments.get("gameDir"));
        Log.debug(LogCategory.GAME_PROVIDER, "Launch directory is " + launchDir);
    }

    /**
     * You can use this method to do initialization stuff.
     * At this point fabric is also ready to modify bytecode so it's time to run your patches.
     * <br><br>
     * This method is called after {@link #locateGame(FabricLauncher, String[])}
     * once fabric-loader has prepared the class loader for injection.
     * @see <a href="https://github.dev/FabricMC/fabric-loader/blob/6adfe08efeb04c8dde829053e3cc546c01ef8415/src/main/java/net/fabricmc/loader/impl/launch/knot/Knot.java#L130">Knot.java#L130</a>
     */
    @Override
    public void initialize(FabricLauncher launcher) {
        // This will run your patches against the gameJar
        // I don't fully understand it
        launcher.setValidParentClassPath(validParentClassPath);
        TRANSFORMER.locateEntrypoints(launcher, Collections.singletonList(gameJar));
    }

    /**
     * Just a simple getter.
     */
    @Override
    public GameTransformer getEntrypointTransformer() {
        return TRANSFORMER;
    }

    /**
     * Not sure what this does or why it's needed.
     * But you should add all game jars to the classpath.
     * If I had to guess I'ld say it limits which classes can be injected into
     */
    @Override
    public void unlockClassPath(FabricLauncher launcher) {
        launcher.addToClassPath(gameJar);
    }

    /**
     * In this method you must call the game's {@code main}
     */
    @Override
    public void launch(ClassLoader loader) {
        String targetClass;

        // If your entrypoint also contains the main method then you don't need this,
        if (entrypoint.equals(CLIENT_ENTRYPOINT)) {
            targetClass = CLIENT_MAIN;
        } else if (entrypoint.equals(SERVER_ENTRYPOINT)) {
            targetClass = SERVER_MAIN;
        } else {
            // Should be impossible
            throw new RuntimeException("Unknown entrypoint " + entrypoint + ".");
        }

        try {
            Class<?> c = loader.loadClass(targetClass);
            Method m = c.getMethod("main", String[].class);
            m.invoke(null, (Object) arguments.toArray());
        } catch (InvocationTargetException e) {
            throw new FormattedException("The game has crashed!", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new FormattedException("Failed to start the game", e);
        }
    }

    /**
     * Just a simple getter.
     */
    @Override
    public Arguments getArguments() {
        return arguments;
    }

    /**
     * This is copied directly from <a href="https://github.dev/FabricMC/fabric-loader/blob/6adfe08efeb04c8dde829053e3cc546c01ef8415/minecraft/src/main/java/net/fabricmc/loader/impl/game/minecraft/MinecraftGameProvider.java#L380">MinecraftGameProvider.java#L380</a>
     * <br>
     * @see FabricLoader#getLaunchArguments(boolean)
     */
    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        if (arguments == null) return new String[0];

        String[] ret = arguments.toArray();
        if (!sanitize) return ret;

        int writeIdx = 0;

        for (int i = 0; i < ret.length; i++) {
            String arg = ret[i];

            if (i + 1 < ret.length
                    && arg.startsWith("--")
                    && SENSITIVE_ARGS.contains(arg.substring(2).toLowerCase(Locale.ENGLISH))) {
                i++; // skip value
            } else {
                ret[writeIdx++] = arg;
            }
        }

        if (writeIdx < ret.length) ret = Arrays.copyOf(ret, writeIdx);
        return ret;
    }
}
