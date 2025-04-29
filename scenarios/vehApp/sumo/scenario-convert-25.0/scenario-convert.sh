#!/bin/bash
set -e

# check if JAVA_HOME is set and define path to java binary accordingly (variable: java_bin)
if [ -z "${JAVA_HOME}" ]; then
  java_bin=java
else
  java_bin=${JAVA_HOME}/bin/java
fi

# mosaic
dir_tools=tools
tmp=`ls ${dir_tools} | grep scenario-convert-.*.jar`
tools=${dir_tools}/${tmp//[^A-Za-z0-9\-\.]}

# create and run command
"${java_bin}" -cp ${tools}:lib/mosaic/*:lib/extended/*:lib/third-party/* com.dcaiti.mosaic.tools.scenarioconvert.core.Starter "$@"