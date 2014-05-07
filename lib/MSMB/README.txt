Multi-State Model Builder (MSMB)
==

----
System requirements:
- CMake (2.8.6 or newer)
- 2.8.6 has adequate Java support.
- Make sure CMake can find Java on your system.
----
	
Windows Instructions

Setup GnuWin32 tools:
	1. Go to http://sourceforge.net/projects/getgnuwin32/files/ and download the GetGnuWin32 executable installer.
	
	2. Run the installer.
	
	3. After the installer finishes, open a command prompt and navigate to the newly created GetGnuWin32 folder. Type "download", this will download any updates that have been made. Next type "install <installation directory>", where <installation directory> is the directory where the gnuwin32 tools will be installed.

		For example, if the executable installer put the GetGnuWin32 on the desktop you should do the following:

		C:\Users\user1\Desktop> cd GetGnuWin32
		C:\Users\user1\Desktop\GetGnuWin32> download
		C:\Users\user1\Desktop\GetGnuWin32> install C:\gnuwin32

		After the appropriate files are unzipped and organized, you will be informed that some of the packages in gnuwin32 project are outdated. When prompted "Would you like more information?", type "yes". A list of updated packages is then shown. 

		You will then be prompted "Would you like to install these utilities?", type "yes". 

		Next, you will be asked which directory to install the updated packages. The default C:\gnuwin32\bin directory is selected by pressing enter, please press enter.

	These steps will install the GnuWin32 tools data in the C:\gnuwin32 directory.
	
	4. Next, you need to append the directory C:\gnuwin32\bin to your system PATH. This can be accomplished by editing the Environment Variables on your system.
		

Setup CMake:
	1. Go to http://www.cmake.org/cmake/resources/software.html and download the Windows executable installer.
	
	2. Run the installer.
		a. When prompted, select "Add CMake to the system PATH for all users"
		
	3. When using CMake the first time, it is recommended to use the GUI version. Run CMake(cmake-gui) located at Start menu -> Programs -> CMake -> CMake(cmake-gui).
	
	4. Set the source code to the directory where CMakeLists.txt is located, which is the MSMB folder.
	
	5. Set the build directory to MSMB\build.
	
	6. Press the Configure button at the bottom. When prompted, select "Unix Makefiles" as the generator and use the default native compilers. There may be many variables listed in red in the top panel, that is ok.
	
	7. Press the configure button again. If any of the variables remains red, then CMake was unable to find the location of the variables. For example, if CMAKE_Java_RUNTIME is still red then CMake was unable to find java.exe on the machine. Please fill in the locations of any missing variables.
	
	8. Click generate.
	
	9. Open up a command prompt and navigate to the MSMB\build folder. To build the project and create the MSMB.jar type:
		make
		
	10. To run the jar, type:
		java -jar MSMB.jar
	
	Note: After the initial setup with CMake(cmake-gui), CMake can be run from the command prompt. To do this:
		1. Open a command prompt.
		
		2. Navigate to MSMB\build.
		
		3. Type:
			cmake -G "Unix Makefiles" ..
			
		4. Once this is done, you can build the project and create the MSMB.jar by typing:
			make
