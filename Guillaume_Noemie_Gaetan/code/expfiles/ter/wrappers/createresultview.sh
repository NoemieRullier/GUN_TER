#!/bin/bash
# $2 : path of wrapper
# $1 : path of view in sparql
# $3 : path of the result
# $1 and $3 are the arguments of wrapper constructor
# Must be in the wrapper directory

GUNPATH=`pwd | rev | cut -d"/" -f 5- | rev`

vs=`echo $1 | sed "s|GUNPATH|$GUNPATH|"`
w=`echo $2 | sed "s|GUNPATH|$GUNPATH|"`
vo=`echo $3 | sed "s|GUNPATH|$GUNPATH|"`
source=`echo $4 | sed "s|GUNPATH|$GUNPATH|"`

echo $vs
echo $w
echo $vo
echo $source

#javac -cp ".:../../../queryExecutor/lib2/*" -sourcepath "$source" $w.java
java -cp ".:../../../queryExecutor/lib2/*:$source*" $w $vs $vo