#!/bin/bash
# $2 : path of wrapper
# $1 : path of view in sparql
# $3 : path of the result
# $1 and $3 are the arguments of wrapper constructor

echo $0
echo $1
echo $2
echo $3
javac -cp ".:../lib2/*" $2
java -cp $2 @$1 @$3