#!/bin/bash

# $1 : path of the wrapper
# $2 : path of the sparql view
# $3 : path of the result
# $2 and $3 are the arguments of wrapper constructor
# Must be in the wrapper directory
#/home/guillaume/Bureau/M1Sem2/recherche/GUN_TER_git/Guillaume_Noemie_Gaetan/code/expfiles/ter/wrappers/

#GUNPATH=`pwd | rev | cut -d"/" -f 4- | rev`
#GUNPATH2=`pwd | rev | cut -d"/" -f 5- | rev | cut -d"/" -f 2-`

#echo $GUNPATH

#echo $GUNPATH2
#echo "youpi"

wrapperPath=`echo $1 | sed "s|GUNPATH|$GUNPATH|"`
sparqlViewPath=`echo $2 | sed "s|GUNPATH|$GUNPATH|"`
resultPath=`echo $3 | sed "s|GUNPATH|$GUNPATH|"`
#source=`echo $4 | sed "s|GUNPATH|$GUNPATH|"`

echo $wrapperPath
echo $sparqlViewPath
echo $resultPath
#echo $source
#libpath="$GUNPATH""/code/queryExecutor/lib2/"
#echo $libpath

#javac -cp ".:../../../queryExecutor/lib2/*" -sourcepath "$source" $w.java
#java -cp ".:../../../queryExecutor/lib2/*:$source*" $w $vs $vo
java -jar $wrapperPath $sparqlViewPath $resultPath
