# This helps CMake create an executable JAR file.
# The created JAR references external JAR libraries. 
# The paths to the external JARs are included in the MANIFEST file.

function(create_jar _TARGET_NAME)
    set(_JAVA_SOURCE_FILES ${ARGN})
	
	#message("Compiling on OS: " ${CMAKE_HOST_SYSTEM_NAME})
	#message("CMAKE_HOST_SYSTEM_VERSION "${CMAKE_HOST_SYSTEM_VERSION})
	#message("CMAKE_HOST_SYSTEM_PROCESSOR: " ${CMAKE_HOST_SYSTEM_PROCESSOR})
	#message("CMAKE_HOST_SYSTEM" ${CMAKE_HOST_SYSTEM})
	#message("PROCESSOR_ARCHITEW6432: " $ENV{PROCESSOR_ARCHITEW6432})
	#message("PROCESSOR_ARCHITECTURE: " $ENV{PROCESSOR_ARCHITECTURE})
	
	if("${CMAKE_HOST_SYSTEM_NAME}" MATCHES "Windows")
		set(COPASI_LIBRARY_NAME "CopasiJava.dll")
		 message("Windows detected!!")
		 set(COPASI_DIR_OS "win")
		 if($ENV{PROCESSOR_ARCHITEW6432} MATCHES "64")
				message("64!!")
				set(COPASI_DIR_ARCH 64)
		elseif($ENV{PROCESSOR_ARCHITEW6432} MATCHES "64")
				message("32!!")
				set(COPASI_DIR_ARCH 32)
		endif( $ENV{PROCESSOR_ARCHITEW6432} MATCHES "64")
	endif("${CMAKE_HOST_SYSTEM_NAME}" MATCHES "Windows")
	
	if("${CMAKE_HOST_SYSTEM_NAME}" MATCHES "Linux")
		set(COPASI_LIBRARY_NAME "libCopasiJava.so")
		 message("Linux detected!!")
		 set(COPASI_DIR_OS "linux")
		 if("${CMAKE_HOST_SYSTEM_PROCESSOR}" MATCHES "64")
			message("64!!")
			set(COPASI_DIR_ARCH 64)
		else ("${CMAKE_HOST_SYSTEM_PROCESSOR}" MATCHES "64")
				message("32!!")
				set(COPASI_DIR_ARCH 32)
		endif("${CMAKE_HOST_SYSTEM_PROCESSOR}" MATCHES "64")	
	endif("${CMAKE_HOST_SYSTEM_NAME}" MATCHES "Linux")
	
	if("${CMAKE_HOST_SYSTEM_NAME}" MATCHES "Darwin")
		 message("Darwin detected!!")
		 set(COPASI_DIR_OS "mac_universal")
	endif("${CMAKE_HOST_SYSTEM_NAME}" MATCHES "Darwin")
	
	#######################
	 
    if (NOT DEFINED CMAKE_JAVA_TARGET_OUTPUT_DIR)
      set(CMAKE_JAVA_TARGET_OUTPUT_DIR ${CMAKE_CURRENT_BINARY_DIR})
    endif(NOT DEFINED CMAKE_JAVA_TARGET_OUTPUT_DIR)

	file(COPY ${MSMB_SOURCE_DIR}/libs DESTINATION 
                             ${CMAKE_JAVA_TARGET_OUTPUT_DIR})
	#Copy the correct COPASI library in the directory where the jar is
		
	file(COPY ${MSMB_SOURCE_DIR}/CopasiLibs/${COPASI_DIR_OS}${COPASI_DIR_ARCH}/${COPASI_LIBRARY_NAME} DESTINATION 
                             ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/libs)	
	file(COPY ${MSMB_SOURCE_DIR}/CopasiLibs/${COPASI_DIR_OS}${COPASI_DIR_ARCH}/copasi.jar DESTINATION 
                             ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/libs)	
							 
	file(GLOB MY_JARS "${CMAKE_JAVA_TARGET_OUTPUT_DIR}/libs/*.jar") 
	set(CMAKE_JAVA_INCLUDE_PATH ${MY_JARS})

    if (CMAKE_JAVA_JAR_ENTRY_POINT)
      set(_ENTRY_POINT_OPTION e)
      set(_ENTRY_POINT_VALUE ${CMAKE_JAVA_JAR_ENTRY_POINT})
    endif (CMAKE_JAVA_JAR_ENTRY_POINT)

    if (LIBRARY_OUTPUT_PATH)
        set(CMAKE_JAVA_LIBRARY_OUTPUT_PATH ${LIBRARY_OUTPUT_PATH})
    else (LIBRARY_OUTPUT_PATH)
        set(CMAKE_JAVA_LIBRARY_OUTPUT_PATH ${CMAKE_JAVA_TARGET_OUTPUT_DIR})
    endif (LIBRARY_OUTPUT_PATH)

    set(CMAKE_JAVA_INCLUDE_PATH
        ${CMAKE_JAVA_INCLUDE_PATH}
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_JAVA_OBJECT_OUTPUT_PATH}
        ${CMAKE_JAVA_LIBRARY_OUTPUT_PATH}
    )

    if (WIN32 AND NOT CYGWIN)
        set(CMAKE_JAVA_INCLUDE_FLAG_SEP ";")
    else (WIN32 AND NOT CYGWIN)
        set(CMAKE_JAVA_INCLUDE_FLAG_SEP ":")
    endif(WIN32 AND NOT CYGWIN)

    foreach (JAVA_INCLUDE_DIR ${CMAKE_JAVA_INCLUDE_PATH})
       set(CMAKE_JAVA_INCLUDE_PATH_FINAL "${CMAKE_JAVA_INCLUDE_PATH_FINAL}${CMAKE_JAVA_INCLUDE_FLAG_SEP}${JAVA_INCLUDE_DIR}")
    endforeach(JAVA_INCLUDE_DIR)

    set(CMAKE_JAVA_CLASS_OUTPUT_PATH "${CMAKE_JAVA_TARGET_OUTPUT_DIR}${CMAKE_FILES_DIRECTORY}/${_TARGET_NAME}.dir")

    set(_JAVA_TARGET_OUTPUT_NAME "${_TARGET_NAME}.jar")
    if (CMAKE_JAVA_TARGET_OUTPUT_NAME AND CMAKE_JAVA_TARGET_VERSION)
        set(_JAVA_TARGET_OUTPUT_NAME "${CMAKE_JAVA_TARGET_OUTPUT_NAME}-${CMAKE_JAVA_TARGET_VERSION}.jar")
        set(_JAVA_TARGET_OUTPUT_LINK "${CMAKE_JAVA_TARGET_OUTPUT_NAME}.jar")
    elseif (CMAKE_JAVA_TARGET_VERSION)
        set(_JAVA_TARGET_OUTPUT_NAME "${_TARGET_NAME}-${CMAKE_JAVA_TARGET_VERSION}.jar")
        set(_JAVA_TARGET_OUTPUT_LINK "${_TARGET_NAME}.jar")
    elseif (CMAKE_JAVA_TARGET_OUTPUT_NAME)
        set(_JAVA_TARGET_OUTPUT_NAME "${CMAKE_JAVA_TARGET_OUTPUT_NAME}.jar")
    endif (CMAKE_JAVA_TARGET_OUTPUT_NAME AND CMAKE_JAVA_TARGET_VERSION)
    # reset
    set(CMAKE_JAVA_TARGET_OUTPUT_NAME)

    set(_JAVA_CLASS_FILES)
    set(_JAVA_COMPILE_FILES)
    set(_JAVA_DEPENDS)
    set(_JAVA_RESOURCE_FILES)
    foreach(_JAVA_SOURCE_FILE ${_JAVA_SOURCE_FILES})
        get_filename_component(_JAVA_EXT ${_JAVA_SOURCE_FILE} EXT)
        get_filename_component(_JAVA_FILE ${_JAVA_SOURCE_FILE} NAME_WE)
        get_filename_component(_JAVA_PATH ${_JAVA_SOURCE_FILE} PATH)
        get_filename_component(_JAVA_FULL ${_JAVA_SOURCE_FILE} ABSOLUTE)

	    file(RELATIVE_PATH _JAVA_REL_BINARY_PATH ${CMAKE_JAVA_TARGET_OUTPUT_DIR} ${_JAVA_FULL})
        file(RELATIVE_PATH _JAVA_REL_SOURCE_PATH ${CMAKE_CURRENT_SOURCE_DIR} ${_JAVA_FULL})
        string(LENGTH ${_JAVA_REL_BINARY_PATH} _BIN_LEN)
        string(LENGTH ${_JAVA_REL_SOURCE_PATH} _SRC_LEN)
        if (${_BIN_LEN} LESS ${_SRC_LEN})
            set(_JAVA_REL_PATH ${_JAVA_REL_BINARY_PATH})
        else (${_BIN_LEN} LESS ${_SRC_LEN})
            set(_JAVA_REL_PATH ${_JAVA_REL_SOURCE_PATH})
        endif (${_BIN_LEN} LESS ${_SRC_LEN})
        get_filename_component(_JAVA_REL_PATH ${_JAVA_REL_PATH} PATH)

        if (_JAVA_EXT MATCHES ".java")
            list(APPEND _JAVA_COMPILE_FILES ${_JAVA_SOURCE_FILE})
            set(_JAVA_CLASS_FILE "${CMAKE_JAVA_CLASS_OUTPUT_PATH}/${_JAVA_REL_PATH}/${_JAVA_FILE}.class")
            set(_JAVA_CLASS_FILES ${_JAVA_CLASS_FILES} ${_JAVA_CLASS_FILE})

        elseif (_JAVA_EXT MATCHES ".jar"
                OR _JAVA_EXT MATCHES ".war"
                OR _JAVA_EXT MATCHES ".ear"
                OR _JAVA_EXT MATCHES ".sar")
            list(APPEND CMAKE_JAVA_INCLUDE_PATH ${_JAVA_SOURCE_FILE})

        elseif (_JAVA_EXT STREQUAL "")
            list(APPEND CMAKE_JAVA_INCLUDE_PATH ${JAVA_JAR_TARGET_${_JAVA_SOURCE_FILE}} ${JAVA_JAR_TARGET_${_JAVA_SOURCE_FILE}_CLASSPATH})
            list(APPEND _JAVA_DEPENDS ${JAVA_JAR_TARGET_${_JAVA_SOURCE_FILE}})

        else (_JAVA_EXT MATCHES ".java")
            __java_copy_file(${CMAKE_CURRENT_SOURCE_DIR}/${_JAVA_SOURCE_FILE}
                             ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/${_JAVA_SOURCE_FILE}
                             "Copying ${_JAVA_SOURCE_FILE} to the build directory")
            list(APPEND _JAVA_RESOURCE_FILES ${_JAVA_SOURCE_FILE})
        endif (_JAVA_EXT MATCHES ".java")
    endforeach(_JAVA_SOURCE_FILE)

	#Copy the images directory in the OUTPUT path
	 file(COPY ${MSMB_SOURCE_DIR}/src/msmb/gui/images/ DESTINATION 
                             ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/msmb/gui/images)
	#Copy the version file in the OUTPUT path
	 file(COPY ${MSMB_SOURCE_DIR}/version.txt DESTINATION 
                             ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/msmb/gui/images)

	
	


    # create an empty java_class_filelist
    if (NOT EXISTS ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist)
        file(WRITE ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist "")
    endif()
	
	 
	if (_JAVA_COMPILE_FILES)
        # Compile the java files and create a list of class files
		add_custom_command(
            # NOTE: this command generates an artificial dependency file
            OUTPUT ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_compiled_${_TARGET_NAME}
            COMMAND ${Java_JAVAC_EXECUTABLE}
                ${CMAKE_JAVA_COMPILE_FLAGS}
				-classpath "${CMAKE_JAVA_INCLUDE_PATH_FINAL}"
                -d ${CMAKE_JAVA_CLASS_OUTPUT_PATH}
                ${_JAVA_COMPILE_FILES}
            COMMAND ${CMAKE_COMMAND} -E touch ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_compiled_${_TARGET_NAME}
            DEPENDS ${_JAVA_COMPILE_FILES}
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
            COMMENT "Building Java objects for ${_TARGET_NAME}.jar"
        )
        add_custom_command(
            OUTPUT ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist
            COMMAND ${CMAKE_COMMAND}
                -DCMAKE_JAVA_CLASS_OUTPUT_PATH=${CMAKE_JAVA_CLASS_OUTPUT_PATH}
                -DCMAKE_JAR_CLASSES_PREFIX="${CMAKE_JAR_CLASSES_PREFIX}"
                -P ${_JAVA_CLASS_FILELIST_SCRIPT}
            DEPENDS ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_compiled_${_TARGET_NAME}
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        )
		else (_JAVA_EXT MATCHES ".java")
    endif (_JAVA_COMPILE_FILES)
	#Copy the version file in the LIBS path in the same path as the finaly .jar, because that is the setup for the web installation using IzPack
	
	
							 
    # create the manifest file for the jar file
	# (because the libs directory in this case is in the same folder as the jar, while in the GitHub case, is downloaded in the partent folder)
    set(ManifestInfo 
	"Manifest-Version: 1.0
Class-Path: . libs/RCaller-2.1.0-SNAPSHOT.jar libs/autocomplete.jar libs/biomodels-wslib_standalone-1.21.jar libs/commons-lang3-3.1.jar libs/copasi.jar libs/djep-full-latest.jar libs/guava-r09.jar libs/itextpdf-5.2.1.jar libs/junit.jar libs/lablib-checkboxtree-3.2.jar libs/swingx-all-1.6.3.jar
Main-Class: msmb.gui.MainGui
"
)

    set(_JAVA_JAR_MANIFEST_PATH ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/MANIFEST.MF)
    file(WRITE ${_JAVA_JAR_MANIFEST_PATH} ${ManifestInfo})

    # create the jar file
    set(_JAVA_JAR_OUTPUT_PATH
      ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/${_JAVA_TARGET_OUTPUT_NAME})

	  
						 
	 
    if (CMAKE_JNI_TARGET)
        add_custom_command(
            OUTPUT ${_JAVA_JAR_OUTPUT_PATH}
            COMMAND ${Java_JAR_EXECUTABLE}
                -cf${_ENTRY_POINT_OPTION} ${_JAVA_JAR_OUTPUT_PATH} ${_ENTRY_POINT_VALUE}
                ${_JAVA_RESOURCE_FILES} @java_class_filelist msmb/gui/images
            COMMAND ${CMAKE_COMMAND}
                -D_JAVA_TARGET_DIR=${CMAKE_JAVA_TARGET_OUTPUT_DIR}
                -D_JAVA_TARGET_OUTPUT_NAME=${_JAVA_TARGET_OUTPUT_NAME}
                -D_JAVA_TARGET_OUTPUT_LINK=${_JAVA_TARGET_OUTPUT_LINK}
                -P ${_JAVA_SYMLINK_SCRIPT}
            COMMAND ${CMAKE_COMMAND}
                -D_JAVA_TARGET_DIR=${CMAKE_JAVA_TARGET_OUTPUT_DIR}
                -D_JAVA_TARGET_OUTPUT_NAME=${_JAVA_JAR_OUTPUT_PATH}
                -D_JAVA_TARGET_OUTPUT_LINK=${_JAVA_TARGET_OUTPUT_LINK}
                -P ${_JAVA_SYMLINK_SCRIPT}
            DEPENDS ${_JAVA_RESOURCE_FILES} ${_JAVA_DEPENDS} ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist
            WORKING_DIRECTORY ${CMAKE_JAVA_CLASS_OUTPUT_PATH}
            COMMENT "Creating Java archive ${_JAVA_TARGET_OUTPUT_NAME}"
        )
    else ()
        add_custom_command(
            OUTPUT ${_JAVA_JAR_OUTPUT_PATH}
            COMMAND ${Java_JAR_EXECUTABLE}
                -cfm ${_JAVA_JAR_OUTPUT_PATH} ${_JAVA_JAR_MANIFEST_PATH}
                ${_JAVA_RESOURCE_FILES} @java_class_filelist msmb/gui/images
            COMMAND ${CMAKE_COMMAND}
                -D_JAVA_TARGET_DIR=${CMAKE_JAVA_TARGET_OUTPUT_DIR}
                -D_JAVA_TARGET_OUTPUT_NAME=${_JAVA_TARGET_OUTPUT_NAME}
                -D_JAVA_TARGET_OUTPUT_LINK=${_JAVA_TARGET_OUTPUT_LINK}
                -P ${_JAVA_SYMLINK_SCRIPT}
            WORKING_DIRECTORY ${CMAKE_JAVA_CLASS_OUTPUT_PATH}
            DEPENDS ${_JAVA_RESOURCE_FILES} ${_JAVA_DEPENDS} ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist
            COMMENT "Creating Java archive ${_JAVA_TARGET_OUTPUT_NAME}"
        )
    endif (CMAKE_JNI_TARGET)

    # Add the target and make sure we have the latest resource files.
    add_custom_target(${_TARGET_NAME} ALL DEPENDS ${_JAVA_JAR_OUTPUT_PATH})

    set_property(
        TARGET
            ${_TARGET_NAME}
        PROPERTY
            INSTALL_FILES
                ${_JAVA_JAR_OUTPUT_PATH}
    )

    if (_JAVA_TARGET_OUTPUT_LINK)
        set_property(
            TARGET
                ${_TARGET_NAME}
            PROPERTY
                INSTALL_FILES
                    ${_JAVA_JAR_OUTPUT_PATH}
                    ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/${_JAVA_TARGET_OUTPUT_LINK}
        )

        if (CMAKE_JNI_TARGET)
            set_property(
                TARGET
                    ${_TARGET_NAME}
                PROPERTY
                    JNI_SYMLINK
                        ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/${_JAVA_TARGET_OUTPUT_LINK}
            )
        endif (CMAKE_JNI_TARGET)
    endif (_JAVA_TARGET_OUTPUT_LINK)

    set_property(
        TARGET
            ${_TARGET_NAME}
        PROPERTY
            JAR_FILE
                ${_JAVA_JAR_OUTPUT_PATH}
    )

    set_property(
        TARGET
            ${_TARGET_NAME}
        PROPERTY
            CLASSDIR
                ${CMAKE_JAVA_CLASS_OUTPUT_PATH}
    )

	
# on linux and MAC, for unknown reasons, the addLibrary from inside the MSMB does not seem to work
# so we need to explicitly set the library path to the libs folder.
# We created a "launcher" script that set the path according to the compilation setup.
# If the final directory is moved, it is the user responsibility to change the parameter in the script accordingly.
 if(${COPASI_DIR_OS} MATCHES "linux")
              execute_process(COMMAND echo "export LD_LIBRARY_PATH=./libs:$LD_LIBRARY_PATH \njava -jar" ${_TARGET_NAME}.jar
WORKING_DIRECTORY ${CMAKE_JAVA_TARGET_OUTPUT_DIR}
OUTPUT_FILE "${_TARGET_NAME}_launcher.sh")
endif(${COPASI_DIR_OS} MATCHES "linux")

if(${CMAKE_HOST_SYSTEM_NAME} MATCHES "Darwin")
	#Copy the main script to reset the working directory of the launcher file 
	file(COPY ${MSMB_SOURCE_DIR}/CopasiLibs/${COPASI_DIR_OS}${COPASI_DIR_ARCH}/scriptToRunJar.txt 
		DESTINATION ${CMAKE_JAVA_TARGET_OUTPUT_DIR}
		FILE_PERMISSIONS OWNER_READ OWNER_WRITE OWNER_EXECUTE GROUP_READ)
	file(RENAME ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/scriptToRunJar.txt ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/${_TARGET_NAME}_launcher.command )
 	file(APPEND ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/${_TARGET_NAME}_launcher.command "\njava -Djava.library.path=./libs -jar ${_TARGET_NAME}.jar")
endif(${CMAKE_HOST_SYSTEM_NAME} MATCHES "Darwin")

endfunction(create_jar)
