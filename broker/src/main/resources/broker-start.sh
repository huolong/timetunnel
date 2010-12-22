#!/bin/bash

SCRIPT_NAME=$0;
BIN_DIR=`dirname ${SCRIPT_NAME}`;
BASE_DIR="${BIN_DIR}/..";
BASE_LIB=${BASE_DIR}/lib;
BASE_CONF=${BASE_DIR}/conf;
BASE_LOG=${BASE_DIR}/log;

PID_FILE=${BASE_CONF}/.timetunnel.broker.pid;

if [ -f $PID_FILE ];
then
        old_pid=`cat $PID_FILE`;
        pids=`ps aux | grep java | awk '{print $2;}'`;
        for pid in $pids
        do
                if [ $pid -eq $old_pid ];
                then
                        echo "process is running as $pid,please stop it first.";
                        exit 0;
                fi
        done
fi

# VM Options : -XX:NewRatio=1 -XX:+PrintGCTimeStamps -XX:+PrintGCDetails
script="java -XX:NewRatio=1 -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -XX:HeapDumpPath=${BASE_LOG}/broker.hprof -XX:ErrorFile=${BASE_LOG}/crash.log -XX:+PrintCommandLineFlags -Xmx2048m -Xms2048m -XX:+HeapDumpOnOutOfMemoryError -Dtt.log.file=${BASE_LOG}/broker.log -Dlog4j.configuration=file:${BASE_CONF}/log4j.properties -classpath ${BASE_LIB}/*: com.taobao.timetunnel.bootstrap.BrokerBootstrap ${BASE_CONF}/conf.properties";
echo $script
nohup $script &
pid=$!
echo $pid > $PID_FILE;

