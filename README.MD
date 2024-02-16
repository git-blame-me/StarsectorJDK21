# Setup

1. Unzip release into /Starsector
2. Download JDK21 https://jdk.java.net/21/ 
3. Rename the /Starsector/jre folder (or delete it)
4. Create a new /Starsector/jre folder
5. Unpack JDK21 into that folder so that you have /Starsector/jre/bin
6. Enjoy the new JDK21

**The included vmparams file sets the max memory to 2G, it also disables verification which is needed since Starsector's obfuscator is bad.
*** Tested for Starsector 0.96 and 0.97, obviously this can be very sensitive to changes on the starsector side.

# Why?

I wanted to do some modding but was irritated by having to write code for ancient java.

Maybe people can test it and see if it is better or worse? New garbage collectors are available as well:\
https://medium.com/@asdf123456789/a-new-modern-garbage-collector-in-jdk-21-4087e7ea9a5c \
https://openjdk.org/jeps/377 \
https://openjdk.org/jeps/439 

# What does this do?

## First, new libraries.
1. byte-buddy-1.14.11.jar - This is for the java agent described below.
2. jaxb-api.jar / txw2.jar - This is an older version of jaxb, 2.3.1. In Java 9 they hid and then later removed EE components https://openjdk.org/jeps/320 so these add needed functionality back.

## Second, this uses a Java agent to edit code as it is loaded so that it works with the newer JDK.
1. DirectBuffer's Cleaner is a different type and in my testing it doesn't really need to be called anymore. This only needed to modify the static method com.fs.graphics.TextureLoader.o00000 - the entire method now does nothing, this seemed fine by looking the bytecode. Obviously this could have other side effects so fingers crossed.
2. Thread.stop() now throws an exception. This call was ALREADY deprecated in Java7 and should not have been used. More recently it was updated to just throw an exception. The agent makes the call to this method do nothing. Seems fine.
3. Starsector code was using IndentingXMLStreamWriter from EE code to write out the save file. Previously this class was inside the Java core library and located at com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter. Now it is in txw2.jar and located at com.sun.xml.txw2.output.IndentingXMLStreamWriter. Since the class has moved packages the agent remaps the reference in the inner class of com.fs.starfarer.campaign.save.CampaignGameManager$5 that is responsible for creating the XStream instance that writes out the save file. 