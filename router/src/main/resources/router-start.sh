#!/bin/bash

SCRIPT_NAME=$0;
BIN_DIR=`dirname ${SCRIPT_NAME}`;
BASE_DIR="${BIN_DIR}/..";
BASE_LIB=${BASE_DIR}/lib;
BASE_CONF=${BASE_DIR}/conf;

PID_FILE=${BASE_CONF}/.timetunnel.router.pid;

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

script="java -Xmx1096m -Xms1096m -XX:+UseConcMarkSweepGC -Dlog4j.configuration=file:${BASE_CONF}/log4j.properties -classpath ${BASE_CONF}:${BASE_LIB}/* com.taobao.timetunnel2.router.service.ServiceEngine";
echo $script >/tmp/timetunnel.router.start;
nohup $script &
pid=$!
echo $pid > $PID_FILE;
