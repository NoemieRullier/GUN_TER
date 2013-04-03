#!/bin/bash

GUNPATH=`pwd | rev | cut -d"/" -f 4- | rev`
QUERIES="1"

cp configD.properties.base configD.properties
sed -i".bkp" "s|GUNPATH|$GUNPATH|" configD.properties

for i in $QUERIES ;do
sed -i".bkp" "s/query[0-9][0-9]*/query$i/" configD.properties
#timeout 30m
java -XX:MaxHeapSize=2048m -cp ".:../lib2/*" experimentseswc/evaluateQueryThreaded configD.properties
done

rm configD.properties.bkp
rm configD.properties
