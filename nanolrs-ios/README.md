NanoLrs for iOS

The port is based on j2objc and SharkORM.  To use use:

1. Run the code generator to translate java code and generate SharkORM entity classes:

'''
$ ./generate.sh
'''

2. Include Generated/*  

Project setup notes:

This is only needed if you want to develop NanoLrs for iOS itself: if not you can simply include the generated sources in your own xcode project.

Project setup is done largely as per http://j2objc.org/docs/Xcode-Build-Rules.html .  

To open:

* Set J2OBJC_HOME : It seems the best way to do this is to set this in Xcode preferences. Click Xcode, Preferences, Locations, Custom Paths, and then add J2OBJC_HOME and point it to the j2objc installation.

* Open NanoLrsLibiOS.xcworkspace

How it was created:

* Create a new XCode project using the Single View Application template

* Cocoapods were setup using pod init and the SharkORM pod was added

* Framework and libraries added as per http://j2objc.org/docs/Xcode-Build-Rules.html

