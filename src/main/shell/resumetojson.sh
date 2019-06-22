#!/bin/bash

if [[ $# -lt  1 ]]
then
	echo "Usage: $0 resumePath" >&2
	exit 1
fi

path=$1
echo '{'
echo '    "wordAttraction" : 0.8,'
echo '    "resumes" : ['

i=0
for r in `find $path -type f`
do
	if [[ $i -gt 0 ]]
	then
		echo ','
	fi
 
	text=`cat $r`
	echo '        {'
	echo '            "id" : "'$i'",'
	echo '            "text" : "'$text'"'
	echo '        }'
	((i++))
done
echo '    ]'

echo '}'

