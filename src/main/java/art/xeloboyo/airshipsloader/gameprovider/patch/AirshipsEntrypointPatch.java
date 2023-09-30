package art.xeloboyo.airshipsloader.gameprovider.patch;

import java.util.function.Consumer;
import java.util.function.Function;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import art.xeloboyo.airshipsloader.gameprovider.services.AirshipGameHooks;
import art.xeloboyo.airshipsloader.gameprovider.services.AirshipGameProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.game.patch.GamePatch;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

/**
 * This class injects the {@link ModInitializer#onInitialize()} hooks.
 * Where you inject depends on your game.
 * <br>
 * It should, imo, be after the game has loaded the essential systems and assets and before the main loop.
 */
public class AirshipsEntrypointPatch extends GamePatch {
    @Override
    public void process(FabricLauncher launcher, Function<String, ClassReader> classSource,
                        Consumer<ClassNode> classEmitter) {
        // Same as GameProvider#getEntrypoint()
        String entrypoint = launcher.getEntrypoint();
        Log.info(LogCategory.GAME_PATCH, "Entrypoint is " + entrypoint);
        // 'Read'/'Parse' the class
        ClassNode entrypointClazz = readClass(classSource.apply(entrypoint));
        if (entrypointClazz == null) {
            throw new LinkageError("Could not load entrypoint class " + entrypoint + "!");
        }
        Log.info(LogCategory.GAME_PATCH, "Entrypoint class is " + entrypointClazz);

        // Modify it
        if (entrypoint.equals(AirshipGameProvider.CLIENT_ENTRYPOINT)) {
            injectClientHook(entrypointClazz);
        } else if (entrypoint.equals(AirshipGameProvider.SERVER_ENTRYPOINT)) {
            injectServerHook(entrypointClazz);
        } else {
            // Should not be possible
            throw new IllegalArgumentException("Unknown entrypoint " + entrypoint + ".");
        }

        // 'Write' it again
        classEmitter.accept(entrypointClazz);
    }

    /**
     * Injects a call to {@link AirshipGameHooks#initClient()} in {@code init()V}
     */
    private void injectClientHook(ClassNode entrypoint) {
        // find the method "init()V"
        MethodNode initMethod = findMethod(entrypoint, (method) -> {
            Log.info(LogCategory.GAME_PATCH,method.name);
            return method.name.equals("<init>") && method.desc.equals("()V");
        });
        if (initMethod == null) {
            throw new NoSuchMethodError("Could not find init method in " + entrypoint + ".");
        }

        injectTailInsn(initMethod, new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                AirshipGameHooks.INTERNAL_NAME,
                "initClient",
                "()V",
                false));
    }

    /**
     * Injects a call to {@link AirshipGameHooks#initServer()} in {@code init()V}
     */
    private void injectServerHook(ClassNode entrypoint) {
        MethodNode initMethod = findMethod(entrypoint, (method) -> method.name.equals("<init>") && method.desc.equals("()V"));
        if (initMethod == null) {
            throw new NoSuchMethodError("Could not find init method in " + entrypoint + ".");
        }

        injectTailInsn(initMethod, new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                AirshipGameHooks.INTERNAL_NAME,
                "initServer",
                "()V",
                false));
    }

    /**
     * Comparable to @Inject(at = @At("TAIL")) in a mixin class
     * @see org.spongepowered.asm.mixin.injection.points.BeforeFinalReturn#find
     */
    private static void injectTailInsn(MethodNode method, AbstractInsnNode injectedInsn) {
        AbstractInsnNode ret = null;

        // RETURN opcode varies based on return type, thus we calculate what opcode we're actually looking for by inspecting the target method
        int returnOpcode = Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN);
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof InsnNode && insn.getOpcode() == returnOpcode) {
                ret = insn;
            }
        }

        // WAT?
        if (ret == null) {
            throw new RuntimeException("TAIL could not locate a valid RETURN in the target method!");
        }

        method.instructions.insertBefore(ret, injectedInsn);
    }
}
