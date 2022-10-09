package art.xeloboyo.airshipsloader.gameprovider;

import net.fabricmc.api.*;
import net.fabricmc.loader.impl.game.*;

public enum AirshipsLibraries implements LibClassifier.LibraryType {
    AIRSHIPS_CLIENT("com/zarkonnen/airships.Main/class"),
    AIRSHIPS_SERVER("com/zarkonnen/airships.Server/class");

    private final EnvType envType;
    private final String[] classPaths;

    AirshipsLibraries(String path) {
        this(null, new String[]{ path });
    }

    AirshipsLibraries(String... paths) {
        this(null, paths);
    }

    AirshipsLibraries(EnvType env, String... paths) {
        classPaths = paths;
        envType = env;
    }

    @Override
    public boolean isApplicable(EnvType env){
        return envType == null || envType == env;
    }

    @Override
    public String[] getPaths(){
        return classPaths;
    }
}
