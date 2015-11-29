#!/bin/bash
# Root directory of your project
PROJDIR=$( echo "`pwd`" )

CONFIGFILE=$1

PROG=ClusterNode

n=1

cd $PROJDIR
rm -f *.class *.out
javac *.java

cat $CONFIGFILE | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
read line 
numhosts=$( echo $line | awk '{ print $1 }' )
#netid=$( echo $line | awk '{ print $2 }' )
netid=$2
while [[ $n -le numhosts ]] 
do
	read line
	echo $line
	node=$( echo $line | awk '{ print $1 }' )
	hostname=$( echo $line | awk '{ print $2 }' )
	port=$( echo $line | awk '{ print $3 }' )
	if [[ $hostname == dc* ]]		
	then
		n=$(( n + 1 ))
		ssh -o StrictHostKeyChecking=no $netid@$hostname "cd $PROJDIR; java $PROG  $node $CONFIGFILE" &
	fi
done
)
echo "Launch complete"
