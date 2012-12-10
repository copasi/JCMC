cmake_minimum_required (VERSION 2.8.6)
find_package(Java)
project(ACGUI Java)

include(UseJava)
include(JarHelper.cmake)

# the java source files
set(SRC
	${ACGUI_SOURCE_DIR}/src/acgui/AC_GUI.java
	${ACGUI_SOURCE_DIR}/src/acgui/ACGraph.java
	${ACGUI_SOURCE_DIR}/src/acgui/ACGraphComponent.java
	${ACGUI_SOURCE_DIR}/src/acgui/ACMenuListener.java
	${ACGUI_SOURCE_DIR}/src/acgui/ACTreeModelListener.java
	${ACGUI_SOURCE_DIR}/src/acgui/Connection.java
	${ACGUI_SOURCE_DIR}/src/acgui/DrawingBoard.java
	${ACGUI_SOURCE_DIR}/src/acgui/MenuItem.java
	${ACGUI_SOURCE_DIR}/src/acgui/Module.java
	${ACGUI_SOURCE_DIR}/src/acgui/ModuleList.java
	${ACGUI_SOURCE_DIR}/src/acgui/Port.java
	${ACGUI_SOURCE_DIR}/src/acgui/PortType.java
	${ACGUI_SOURCE_DIR}/src/acgui/Printer.java
	${ACGUI_SOURCE_DIR}/src/acgui/TreeView.java
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
