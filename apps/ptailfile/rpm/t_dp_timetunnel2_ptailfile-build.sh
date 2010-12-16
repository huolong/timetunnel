#!/bin/bash
export temppath=$1
cd $temppath/rpm
sed -i  "s/^Release:.*$/Release: "$4"/" $2.spec
sed -i  "s/^Version:.*$/Version: "$3"/" $2.spec
/usr/local/bin/rpm_create -p /home/admin/TimeTunnel -v $3 -r $4 $2.spec -k
mv `find . -name $2*-$3-$4*rpm`  .

