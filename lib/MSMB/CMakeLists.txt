cmake_minimum_required (VERSION 2.8.6)
find_package(Java)
project(MSMB Java)

include(UseJava)
include(JarHelperMSMB.cmake)

# the java source files
set(SRC
	${MSMB_SOURCE_DIR}/src/msmb/gui/*.java
	${MSMB_SOURCE_DIR}/src/msmb/model/*.java
	${MSMB_SOURCE_DIR}/src/msmb/commonUtilities/*.java
	${MSMB_SOURCE_DIR}/src/msmb/commonUtilities/tables/*.java
	${MSMB_SOURCE_DIR}/src/msmb/debugTab/*.java
	${MSMB_SOURCE_DIR}/src/msmb/testing/*.java
	${MSMB_SOURCE_DIR}/src/msmb/utility/*.java
	${MSMB_SOURCE_DIR}/src/msmb/parsers/chemicalReaction/*.java
	${MSMB_SOURCE_DIR}/src/msmb/parsers/chemicalReaction/syntaxtree/*.java
	${MSMB_SOURCE_DIR}/src/msmb/parsers/chemicalReaction/visitor/*.java
	${MSMB_SOURCE_DIR}/src/msmb/parsers/mathExpression/*.java
	${MSMB_SOURCE_DIR}/src/msmb/parsers/mathExpression/syntaxtree/*.java
	${MSMB_SOURCE_DIR}/src/msmb/parsers/mathExpression/visitor/*.java
	${MSMB_SOURCE_DIR}/src/msmb/parsers/multistateSpecies/*.java
	${MSMB_SOURCE_DIR}/src/msmb/parsers/multistateSpecies/syntaxtree/*.java
	${MSMB_SOURCE_DIR}/src/msmb/parsers/multistateSpecies/visitor/*.java
#	${MSMB_SOURCE_DIR}/src/msmb/runManager/*.java
)


#git describe --always --> short id of last commit
#git rev-parse HEAD --> long id of last commit
set (git_cmd "git")
set (git_arg1 "describe") 
set (git_arg2 "--tags")
execute_process(COMMAND ${git_cmd} ${git_arg1} ${git_arg2}
  WORKING_DIRECTORY ${MSMB_SOURCE_DIR}
  RESULT_VARIABLE git_result
  OUTPUT_VARIABLE git_ver
)

file(WRITE version.txt "commit=${git_ver}\n")
file(APPEND version.txt "major=1\n")
file(APPEND version.txt "minor=0\n")


# create an executable jar
create_jar(MSMB ${SRC})
