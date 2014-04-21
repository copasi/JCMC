cmake_minimum_required (VERSION 2.8.6)
find_package(Java)
project(ACGUI Java)

include(UseJava)
include(JarHelper.cmake)
include(ExternalProject)

MESSAGE( STATUS "CMAKE_CURRENT_SOURCE_DIR: "${CMAKE_CURRENT_SOURCE_DIR} )

set(SC ${CMAKE_CURRENT_SOURCE_DIR}/MSMB/*.txt)

MESSAGE( STATUS "txt files: " ${SC} )
#MESSAGE( STATUS "HELLOWORLD_SOURCE_DIR: " ${HELLOWORLD_SOURCE_DIR} )

ExternalProject_Add(
 msmb_jar
 SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/lib/MSMB
 BUILD_COMMAND make
 INSTALL_COMMAND ""
)

# the java source files
set(SRC
	${ACGUI_SOURCE_DIR}/src/acgui/*.java
)

#message(STATUS "java path " ${CMAKE_JAVA_INCLUDE_PATH})
#set(CMAKE_JAVA_INCLUDE_PATH 
#	${ACGUI_SOURCE_DIR}/lib/jgraphx.jar
#	${ACGUI_SOURCE_DIR}/lib/copasi.jar
#)
#message(STATUS "java path " ${CMAKE_JAVA_INCLUDE_PATH})

#git describe --always --> short id of last commit
#git rev-parse HEAD --> long id of last commit
#set (git_cmd "git")
#set (git_arg1 "describe") 
#set (git_arg2 "--tags")
#execute_process(COMMAND ${git_cmd} ${git_arg1} ${git_arg2}
#  WORKING_DIRECTORY ${ACGUI_SOURCE_DIR}
#  RESULT_VARIABLE git_result
#  OUTPUT_VARIABLE git_ver
#)

#file(WRITE version.txt "commit=${git_ver}\n")
#file(APPEND version.txt "major=1\n")
#file(APPEND version.txt "minor=0\n")

# create an executable jar
create_jar(ACGUI ${SRC})
