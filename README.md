HLMPConnect
====================

This Android application use [HLMP] to get a simple chat over an automatic adhoc wireless network.

Requirements
------------
* ROOT access (you can use [Android-Easy-Rooting-Toolkit])

How to build
------------

#### Prerequisites

* Android NDK r4 or above
* Eclipse and ADT plugin (android-10)

#### Build
* To build native components run in the project root directory:

		$ ANDROID_NDK_PATH/ndk-build
 
 
* To build the Android application, import the project into Eclipse and add [Simple-XML]


[HLMP]: https://github.com/fvalverd/High-Level-MANET-Protocol
[Simple-XML]: http://simple.sourceforge.net/
[Android-Easy-Rooting-Toolkit]: https://github.com/fvalverd/Android-Easy-Rooting-Toolkit