#NanoLrs for iOS

The port is based on j2objc and SharkORM.

_Prerequisites_

* j2objc installed with the J2OBJC_HOME environment variable pointing to it. e.g.
```
$ export J2OBJC_HOME=/path/to/j2objc
```
* Android SDK installed with local.properties as created by Android Studio pointing to the sdk.dir or ANDROID_HOME environment variable pointing to it
* Cocoapods installed as per cocoapods.org website

To get started run the code generator to translate java code and generate SharkORM entity classes:
```
$ ./generate.sh
```

### Use in an Xcode project

I'm not really an iOS expert : I guess this would be better as a cocoapod library itself (help welcome). For the moment:

* Add dist/include/NanoLrs-Generated and dist/include/NanoLrs-Objc as groups to your project. For each directory
  (NanoLrs-Generated and NanoLrs-Objc )right click on the project you want to add it to, click add files,
  uncheck copy files if needed and select create groups.
* As with most j2objc projects automatic reference counting should be disabled and selectively re-enabled.
 Select the project (root node) from the left hand side, select the target, then click build phases.
 Add the -fobjc-arc flag to all PersistenceManagerIOS, SrkObj, SharkORM objects (easiest way is to
 filter using the menu at the top right)
* Make sure the project it's being added to is setup as below (inc build rules, libraries, frameworks etc) for it to compile and link.

## Development

Project setup is done largely as per http://j2objc.org/docs/Xcode-Build-Rules.html .

To open:

* Set J2OBJC_HOME : It seems the best way to do this is to set this in Xcode preferences. Click Xcode, Preferences, Locations, Custom Paths, and then add J2OBJC_HOME and point it to the j2objc installation.
* Open NanoLrsLibiOS.xcworkspace
* Add a breakpoint to avoid debugging pauses: This project uses NanoHttpd: NanoHttpd works fine when translated with j2objc except for one annoying issue that causes the debugger to pause. Add a breakpoint to work around this behaviour. Go to breakpoints (click symbol on left panel), Click Debug, Breakpoints,
 Create Symbolic breakpoint.  In Symbol Enter UIApplicationMain, leave module
 and condition blank.  Under action select "Debugger Command", enter
 "process handle SIGUSR2 -n true -p true -s false" and check Automatically
 continue after evaluating actions. See http://stackoverflow.com/questions/10431579/permanently-configuring-lldb-in-xcode-4-3-2-not-to-stop-on-signals/10456557#10456557

How it was created:

* Create a new XCode project using the Single View Application template

* Cocoapods were setup using pod init and the SharkORM pod was added

* Framework and libraries added as per http://j2objc.org/docs/Xcode-Build-Rules.html

