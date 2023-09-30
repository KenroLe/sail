/usr/lib/jvm/java-8-openjdk/bin/java -Dhttps.protocols=TLSv1.2,TLSv1.1,TLSv1 -Dfabric.side=client \
	 -cp  fabric-dependencies/sponge-mixin-0.11.4+mixin.0.8.5.jar:\
fabric-dependencies/asm-tree-9.3.jar:\
fabric-dependencies/tiny-remapper-0.8.1.jar:\
fabric-dependencies/asm-analysis-9.3.jar:\
fabric-dependencies/tiny-mappings-parser-0.3.0+build.17.jar:\
fabric-dependencies/asm-commons-9.3.jar:\
fabric-dependencies/access-widener-2.1.0.jar:\
fabric-dependencies/fabric-loader-0.14.11.jar:\
fabric-dependencies/asm-9.3.jar:\
fabric-dependencies/asm-util-9.3.jar:\
AirshipsGameProvider-1.0.0.jar:\
net.fabricmc.loader.impl.launch.knot.KnotClient:\
AirshipsGameProvider-1.0.0.jar:\
../lib/native/*:\
../lib/* \
 -Djava.library.path=/home/kenro/.steam/steam/steamapps/common/Airships\ Conquer\ the\ Skies/lib/native \
 -Dsteam=true \
net.fabricmc.loader.impl.launch.knot.KnotClient

