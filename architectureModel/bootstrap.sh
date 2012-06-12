#!/bin/bash

categories=$( find taxonomy -maxdepth 1 -mindepth 1 | sed -e 's/.*\/\(.*\)/\1/' )
categoriesDirs=$( find taxonomy -maxdepth 1 -mindepth 1 )
serviceDirs=$( find taxonomy -maxdepth 2 -mindepth 2 )

echo "Categories:" $categories

> settings.gradle
for c in $serviceDirs; do
	echo $c
	touch $c/build.gradle
	echo "include '${c//\//:}'">> settings.gradle
done
