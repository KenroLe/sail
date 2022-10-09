package art.xeloboyo.airshipsloader.gameprovider.services;

import art.xeloboyo.airshipsloader.gameprovider.GameVersion;
import net.fabricmc.loader.impl.util.ExceptionUtil;
import net.fabricmc.loader.impl.util.FileSystemUtil;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * You don't need this class but you need some way to determine the game version.
 * This can be as simple as reading a text file or as complicated as reading constants from a class.
 * This depends on where the game stores the version. If not version information is available in the game files
 * you can also use a hash based approach
 * <br>
 * Here are some other examples:
 * <ul>
 *     <li><a href="https://github.dev/WilderForge/WildermythGameProvider/blob/be2b4659faf4220501ef1a63cee990e3a83950ba/src/main/java/com/wildermods/provider/services/WildermythGameProvider.java#L385">WildermythGameProvider</a></li>
 *     <li><a href="https://github.dev/MiniFabric/MinicraftGameProvider/blob/dba19b59041cde075df93d158034e0707e1a97b8/src/main/java/io/github/pseudodistant/provider/services/GetVersionFromHash.java#L6">MinicraftGameProvider</a></li>
 * <li><a href="https://github.dev/FabricMC/fabric-loader/blob/6adfe08efeb04c8dde829053e3cc546c01ef8415/minecraft/src/main/java/net/fabricmc/loader/impl/game/minecraft/McVersionLookup.java#L72">McVersionLookup.java#L72</a></li>
 * </ul>
 */
public class BlankVersionLookup {
    public static GameVersion getVersionFromGameJar(Path jarPath) {
        GameVersion.Builder builder = new GameVersion.Builder();
        try (FileSystemUtil.FileSystemDelegate fs = FileSystemUtil.getJarFileSystem(jarPath, false)) {
            getVersionFromProperties(fs.get(), builder);
            return builder.build();
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * This method loads the file {@code version.properties} from within the game jar and extracts the version information.
     * <br>
     * <h3>This totally different from game to game! This is just an example!</h3>
     */
    private static void getVersionFromProperties(FileSystem fs, GameVersion.Builder builder) throws IOException {
        Path file = fs.getPath("version.properties");
        if (!Files.isRegularFile(file)) {
            throw new RuntimeException("File version.properties is invalid.");
        }
        Properties props = new Properties();
        props.load(Files.newInputStream(file));

        builder.setType(props.getProperty("type"))
                .setModifier(props.getProperty("modifier"))
                .setNumber(Integer.parseInt(props.getProperty("number")));

        String build = props.getProperty("build");
        if (build.contains(".")) {
            String[] parts = build.split("\\.");
            try {
                builder.setBuild(Integer.parseInt(parts[0]));
                builder.setRevision(Integer.parseInt(parts[1]));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            builder.setBuild(Integer.parseInt(build));
        }
    }
}
