SETUPS="300views"
QUERIES=`seq 1 18`
GUNPATH=`pwd | rev | cut -d"/" -f 4- | rev`
folder=$GUNPATH/code/expfiles/berlinData
outputFolder=$GUNPATH/code/expfiles/berlinOutput
DATASET=FiveThousand

for setup in $SETUPS ;do
    for i in $QUERIES ;do
        java -cp ".:../lib2/*" findViewInstantiations ${folder}/$DATASET/${setup}/500rewritings_query${i} ${folder}/$DATASET/${setup}/usedViewInstantiations
    done
done
