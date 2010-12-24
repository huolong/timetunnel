#!/bin/bash

LIB_PATH=/home/admin/TimeTunnel/savefile/lib
CONF_PATH=/home/admin/TimeTunnel/savefile/conf


CLASSPATH=$LIB_PATH/*:$CONF_PATH

script="java -DROUTER=localhost:9999 -DRPCTIMEOUT=30000 -DAPPNAME=savefile -classpath $CLASSPATH  com.taobao.timetunnel2.savefile.app.SaveFileApp"
echo $script > /tmp/timetunnel.savefile.start
$script >> /tmp/timetunnel.savefile.start
sleep 1
