cmake_minimum_required (VERSION 2.8.6)
find_package(Java)
project(ACGUI Java)

include(UseJava)
include(JarHelper.cmake)

# the java source files
set(SRC
	${ACGUI_SOURCE_DIR}/src/acgui/AC_GUI.java
	${ACGUI_SOURCE_DIR}/src/acgui/ACMenuListener.java
	${ACGUI_SOURCE_DIR}/src/acgui/ACTreeModelListener.java
	${ACGUI_SOURCE_DIR}/src/acgui/DrawingBoard.java
	${ACGUI_SOURCE_DIR}/src/acgui/Module.java
	${ACGUI_SOURCE_DIR}/src/acgui/ModuleList.java
	${ACGUI_SOURCE_DIR}/src/acgui/TreeView.java
)

# tell cmake the path to the jgraphx jar file
set(CMAKE_JAVA_INCLUDE_PATH ${ACGUI_SOURCE_DIR}/lib/jgraphx.jar)

# create an executable jar
create_jar(ACGUI ${SRC})
