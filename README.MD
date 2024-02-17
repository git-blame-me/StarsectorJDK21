# Still a WIP

This is still a work in progress and may not be good or totally working. I'm working on testing still so backup your saves, your mileage may vary.

There does appear to be some stuttering with the default Starsector config. I was able to resolve it on my install by increasing the fps setting to 70 or higher in \Starsector\starsector-core\data\config\settings.json. Disabling vsync also fixed it but caused terrible screen tearing for me. It appears that there is some sort timing issue with how vsync is working that has changed since java8. Still investigating.

# Setup

1. Unzip release into /Starsector
2. Download JDK21 https://jdk.java.net/21/ 
3. Rename the /Starsector/jre folder (or delete it)
4. Create a new /Starsector/jre folder
5. Unpack JDK21 into that folder so that you have /Starsector/jre/bin
6. Enjoy the new JDK21

*** Tested for Starsector 0.96 and 0.97, obviously this can be very sensitive to changes on the starsector side.

# Why?

I wanted to do some modding but was irritated by having to write code for ancient java.

Maybe people can test it and see if it is better or worse? New garbage collectors are available as well:\
https://docs.oracle.com/en/java/javase/21/gctuning/available-collectors.html \
https://openjdk.org/jeps/377 \
https://openjdk.org/jeps/439 

# What does this do?

## First, new libraries.
1. byte-buddy-1.14.11.jar - This is for the java agent described below.
2. jaxb-api.jar / txw2.jar - This is an older version of jaxb, 2.3.1 that still has the old javax classpaths. In Java 9 they hid and then later removed EE components https://openjdk.org/jeps/320 so these add needed functionality back.

## Second, this uses a Java agent to edit code as it is loaded so that it works with the newer JDK.
1. DirectBuffer's Cleaner type has changed from sun.misc.Cleaner to jdk.internal.ref.Cleaner so that class has been remapped.
2. Thread.stop() now throws an exception. This call was ALREADY deprecated in Java7 and should not have been used. More recently it was updated to just throw an exception. The agent makes the call to this method do nothing. Seems fine.
3. Starsector code was using IndentingXMLStreamWriter from EE code to write out the save file. Previously this class was inside the Java core library and located at com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter. Now it is in txw2.jar and located at com.sun.xml.txw2.output.IndentingXMLStreamWriter. Since the class has moved packages the agent remaps the reference in the inner class of com.fs.starfarer.campaign.save.CampaignGameManager$5 that is responsible for creating the XStream instance that writes out the save file. 

## Third, some changes to the vmparams so that it all runs.
1. Disables verification -Xverify:none -- This is needed because the obfuscator starsector uses is terrible and uses illegal names for things like return/for/class/etc.
2. Add exports and opens. Since Java 9 https://openjdk.org/jeps/261 we have modules, kind of cool but mostly a pain. These make it so Starsector can access code in other modules that are by default closed.
3. Add new libs to classpath.
4. Add the agent: -javaagent:agent.jar 
5. Does enable the new ZGC,  -XX:+UseZGC -XX:+ZGenerational
6. Add -Djava.util.Arrays.useLegacyMergeSort=true since there appear to be a few of Starsector's comparators are not appropriately transitive (a > b, b > c, therefore a > c) this breaks the newer TimSort in java.
7. Min/Max memory set to 4G. This was fine for me but will greatly vary for people depending on mods used.