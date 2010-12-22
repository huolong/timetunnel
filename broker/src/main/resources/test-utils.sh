#!/bin/bash

SCRIPT_NAME=$0;
BIN_DIR=`dirname ${SCRIPT_NAME}`;
BASE_DIR="${BIN_DIR}/..";
BASE_LIB=${BASE_DIR}/lib;
BASE_CONF=${BASE_DIR}/conf;
BASE_LOG=${BASE_DIR}/log;


# VM Options : -XX:NewRatio=1 
script="java -Xmx2048m -Xms2048m -XX:HeapDumpPath="${BASE_LOG}/testutils.hprof" -XX:+HeapDumpOnOutOfMemoryError -Dtt.log.file=${BASE_LOG}/testutil.log -Dlog4j.configuration=file:${BASE_CONF}/log4j.properties -classpath ${BASE_LIB}/*: com.taobao.timetunnel.client.TestClient $*";
echo $script 
$script

