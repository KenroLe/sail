package art.xeloboyo.airshipsloader.gameprovider.patch;

import art.xeloboyo.airshipsloader.gameprovider.*;
import art.xeloboyo.airshipsloader.gameprovider.services.*;
import net.fabricmc.loader.impl.game.minecraft.patch.*;
import net.fabricmc.loader.impl.game.patch.GamePatch;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;

public class AirshipsBrandingPatch extends GamePatch {

    private static final int VAR_INDEX = 7;

    @Override
    public void process(FabricLauncher launcher, Function<String, ClassReader> classSource, Consumer<ClassNode> classEmitter) {
        ClassNode menuClass = readClass(classSource.apply("com.zarkonnen.airships.MainMenu"));

        // This is a more complicated example of bytecode maioulation
        //

        boolean applied = false;
        // Search all methods
        for (MethodNode node : menuClass.methods) {
            // for one with the signature "build(Larc/scene/Group;)V" aka. "void build(arc.scene.Group param1)"
            // arc.scene.Group is a class of the Arc engine from the game Mindustry
            if (node.name.equals("render")) {
                Log.debug(LogCategory.GAME_PATCH, "Applying brand name hook to %s::%s", menuClass.name, node.name);

                // Search all instructions
                ListIterator<AbstractInsnNode> it = node.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode insn = it.next();
                    // make sure your ide supports viewing bytecode.
                    // here we are searching for 'LDC "1.1.1"'
                    // and replacing it with our version
                    //
                    if(insn.getOpcode() == Opcodes.LDC && insn instanceof LdcInsnNode ldclsn && ldclsn.cst.equals("1.1.1")){
                        it.set(new LdcInsnNode("1.1.1 " + AirshipGameHooks.getBrand()));
                        //it.next();
                        //it.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        //it.add(Utils.getASMStaticMethodCall(AirshipGameHooks.class,"insertBranding",StringBuilder.class));
                        applied = true;
                        break;
                    }
                    /*
                    // For a variable write (store) instruction
                    // That writes to the VAR_INDEX-th variable in the function
                    if (insn.getOpcode() == Opcodes.ASTORE && insn instanceof VarInsnNode varInsn && varInsn.var == VAR_INDEX) {
                        // Insert the instructions generated by injectedDummyCode()


                        it.add(new VarInsnNode(Opcodes.ALOAD, VAR_INDEX));
                        it.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                AirshipGameHooks.INTERNAL_NAME,
                                "insertBranding",
                                "(Ljava/lang/String;)Ljava/lang/String;",
                                false));
                        it.add(new VarInsnNode(Opcodes.ASTORE, VAR_INDEX));
                        applied = true;
                        break;
                    }*/
                }
            }
        }

        if (applied) {
            classEmitter.accept(menuClass);
        } else {
            Log.warn(LogCategory.GAME_PATCH, "Failed to apply brand name. Instruction not found.");
        }
    }

    // This is dummy code used to see what a piece of bytecode would look like
    // In IntelliJ place your cursor inside the function then go to View > Show Bytecode
    // to see which instructions you need to inject. Make sure you have built the project!
    //
    private static void injectedDummyCode() {
        StringBuilder a = new StringBuilder();
        String version = "";
        AirshipGameHooks.insertBranding(a);
        version = a.toString();
    }
}