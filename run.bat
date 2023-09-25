@echo off
java -Dhttps.protocols=TLSv1.2,TLSv1.1,TLSv1 -XX:+ShowCodeDetailsInExceptionMessages ^
-Dfabric.skipMcProvider=true -Dfabric.side=client @"deps.txt" net.fabricmc.loader.impl.launch.knot.KnotClient
