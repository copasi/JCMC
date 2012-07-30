Aggregation Connector (AC)
==
Aggregation connector tool for hierarchical reaction network models

** NOTE **
Currently compiling with CMake only on Linux.

----
System requirements:
- CMake (2.8.6 or newer)
	- 2.8.6 has adequate Java support.
	- Make sure CMake can find Java on your system.
----


To compile use the command:

	cmake <dir>
then
	make	

<dir> is the AC directory where the CMakeLists.txt file is located.

This will create an executable JAR in the directory where cmake and make were executed.
To run the JAR use the command:

	java -jar ACGUI.jar

Suggestion:
	Clone the AC repository.
	Create a "build" directory in AC.
	Make "AC/build" your current directory.
	Do the following:

	cmake ../
	make
	java -jar ACGUI.jar
