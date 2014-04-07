cmake_minimum_required (VERSION 2.8.6)
find_package(Java)
project(ACGUI Java)

include(UseJava)
include(JarHelper.cmake)

# the java source files
set(SRC
	${ACGUI_SOURCE_DIR}/src/acgui/*.java
)

# tell cmake the path to the jgraphx jar file
#set(CMAKE_JAVA_INCLUDE_PATH ${ACGUI_SOURCE_DIR}/lib/jgraphx.jar)
#set(LIBRARY_OUTPUT_PATH ${ACGUI_SOURCE_DIR}/lib/CopasiJava.dll)
#set(CMAKE_JAVA_INCLUDE_PATH "${ACGUI_SOURCE_DIR}/lib/copasi.jar;${ACGUI_SOURCE_DIR}/lib/jgraphx.jar")

#set(CMAKE_JAVA_INCLUDE_PATH "${ACGUI_SOURCE_DIR}/lib/copasi.jar;${ACGUI_SOURCE_DIR}/lib/jgraphx.jar")
#list(APPEND CMAKE_JAVA_INCLUDE_PATH ${_JAVA_SOURCE_FILE})
# tell cmake the path to the copasi jar file
#set(CMAKE_JAVA_INCLUDE_PATH ${ACGUI_SOURCE_DIR}/lib/copasi.jar)

set(CMAKE_JAVA_INCLUDE_PATH 
	${ACGUI_SOURCE_DIR}/lib/jgraphx.jar
	${ACGUI_SOURCE_DIR}/lib/copasi.jar
)

# create an executable jar
create_jar(ACGUI ${SRC})
