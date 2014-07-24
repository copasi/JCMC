cmake_minimum_required (VERSION 2.8.6)
find_package(Java)
project(AC Java)

include(UseJava)
include(JarHelper.cmake)
include(ExternalProject)

ExternalProject_Add(
 MSMB
 PREFIX MSMB
 SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/lib/MSMB
 BINARY_DIR ${CMAKE_CURRENT_BINARY_DIR}/lib/MSMB
 BUILD_COMMAND make
 INSTALL_COMMAND ""
)

# the java source files
set(SRC
	${AC_SOURCE_DIR}/src/acgui/*.java
)

#message(STATUS "java path " ${CMAKE_JAVA_INCLUDE_PATH})
#set(CMAKE_JAVA_INCLUDE_PATH 
#	${AC_SOURCE_DIR}/lib/jgraphx.jar
#	${AC_SOURCE_DIR}/lib/copasi.jar
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
create_jar(AC ${SRC})
