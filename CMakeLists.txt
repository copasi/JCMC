cmake_minimum_required (VERSION 2.8.6)
find_package(Java)
project(JCMC Java)

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
	${JCMC_SOURCE_DIR}/src/acgui/*.java
)

set (git_cmd "git")
set (git_arg1 "describe") 
set (git_arg2 "--tags")
set (git_arg3 "--long")
execute_process(COMMAND ${git_cmd} ${git_arg1} ${git_arg2} ${git_arg3}
  WORKING_DIRECTORY ${JCMC_SOURCE_DIR}
  OUTPUT_VARIABLE git_version
  OUTPUT_STRIP_TRAILING_WHITESPACE
)

file(WRITE version.txt "JCMC version: ${git_version}")

# create an executable jar
create_jar(JCMC ${SRC})
