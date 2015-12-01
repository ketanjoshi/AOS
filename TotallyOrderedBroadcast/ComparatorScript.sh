#!/bin/sh
CONFIG_FILE=$1
FIRST_FILE=$CONFIG_FILE+"-0.out"
NODE_COUNT=1
comp_value=$?
numhosts=0
cat $CONFIG_FILE | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
read line 
numhosts=$( echo $line | awk '{ print $1 }' ) 
)
while [[ $n -le numhosts ]] 
do
	COMPARE_FILE=($CONFIG_FILE+$NODE_COUNT+".out")
	diff --brief $FIRST_FILE $COMPARE_FILE
	EXIT_CODE=$?
	if [ $EXIT_CODE -eq 1 ]
	then
		echo "files: $FIRST_FILE and $COMPARE_FILE are different"
	fi
done