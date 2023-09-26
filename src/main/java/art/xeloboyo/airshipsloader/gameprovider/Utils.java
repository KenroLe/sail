package art.xeloboyo.airshipsloader.gameprovider;

import art.xeloboyo.airshipsloader.gameprovider.services.*;
import net.fabricmc.loader.impl.util.log.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class Utils{
    public static MethodInsnNode getASMStaticMethodCall(Class c,String method, Class... parameterTypes){
        try{
            return new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            c.getName().replace('.', '/'),
            method,
            Type.getMethodDescriptor(c.getMethod(method, parameterTypes)),
            false);
        }catch(Exception e){
            Log.error(LogCategory.GAME_PATCH,"Unable to find the method " + method +" in " + c);
            return null;
        }
    }
}
