public class Main{
    public static String cmdScript = "java -Dhttps.protocols=TLSv1.2,TLSv1.1,TLSv1 -XX:+ShowCodeDetailsInExceptionMessages ^ -Dfabric.skipMcProvider=true -Dfabric.side=client <args> net.fabricmc.loader.impl.launch.knot.KnotClient";
    public static void main(String args[]){

    }
}
