#!/bin/bash

GUNPATH=`pwd | rev | cut -d"/" -f 4- | rev`

    cp configData.properties.base configData.properties
    sed -i".bkp" "s|GUNPATH|$GUNPATH|" configData.properties

#rm configData.properties
rm configData.properties.bkp
