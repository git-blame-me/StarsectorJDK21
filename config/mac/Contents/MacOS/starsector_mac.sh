#!/bin/bash

DIR="$( cd "$( dirname "$BASH_SOURCE[0]}" )" && pwd )"
echo $DIR  > /tmp/out.txt

cd "$DIR"
cd "../Resources/Java"

export JAVA_HOME=../../Home
"$JAVA_HOME/bin/java" \
    -Xdock:name="Starsector" \
    -Xdock:icon=../../Resources/s_icon128.icns \
    -Dapple.laf.useScreenMenuBar=false \
    -Dcom.apple.macos.useScreenMenuBar=false \
    -Dapple.awt.showGrowBox=false \
    -Dfile.encoding=UTF-8 \
    ${EXTRAARGS} \
	-Djava.library.path=../../Resources/Java/native/macosx \
	-Dcom.fs.starfarer.settings.paths.saves=../../../saves \
	-Dcom.fs.starfarer.settings.paths.screenshots=../../../screenshots \
	-Dcom.fs.starfarer.settings.paths.mods=../../../mods \
	-Dcom.fs.starfarer.settings.paths.logs=../../../logs \
	-Dcom.fs.starfarer.settings.osx=true \
	-Xms4G -Xmx4G -Xss2048k -Xverify:none -verbose:gc -XX:+UseZGC -XX:+ZGenerational \
	--add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
	--add-exports java.base/sun.nio.ch=ALL-UNNAMED \
	--add-opens java.base/java.util=ALL-UNNAMED \
	--add-opens java.base/java.lang.reflect=ALL-UNNAMED \
	--add-opens java.base/java.text=ALL-UNNAMED \
	--add-opens java.desktop/java.awt.font=ALL-UNNAMED \
	--add-opens java.base/java.lang.ref=ALL-UNNAMED \
	-javaagent:agent.jar \
	-Djava.util.Arrays.useLegacyMergeSort=true \
	-cp ../../Resources/Java/byte-buddy-1.14.11.jar:../../Resources/Java/jaxb-api.jar:../../Resources/Java/txw2.jar:../../Resources/Java/AppleJavaExtensions.jar:../../Resources/Java/commons-compiler-jdk.jar:../../Resources/Java/commons-compiler.jar:../../Resources/Java/fs.sound_obf.jar:../../Resources/Java/janino.jar:../../Resources/Java/jinput.jar:../../Resources/Java/jogg-0.0.7.jar:../../Resources/Java/jorbis-0.0.15.jar:../../Resources/Java/json.jar:../../Resources/Java/log4j-1.2.9.jar:../../Resources/Java/lwjgl.jar:../../Resources/Java/lwjgl_util.jar:../../Resources/Java/starfarer.api.jar:../../Resources/Java/starfarer_obf.jar:../../Resources/Java/fs.common_obf.jar:../../Resources/Java/xstream-1.4.10.jar \
    com.fs.starfarer.StarfarerLauncher \
    "$@"

exit 0
