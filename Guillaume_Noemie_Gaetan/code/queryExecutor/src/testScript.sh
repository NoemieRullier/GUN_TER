#!/bin/bash

STRATS="LLP"
SETUPS="75views"
QUERIES="16"
GUNPATH=`pwd | rev | cut -d"/" -f 4- | rev`
PH=$GUNPATH/code/expfiles/berlinOutput

for setup in $SETUPS ;do
    java processResults $PH/$setup output $PH/$setup
done
