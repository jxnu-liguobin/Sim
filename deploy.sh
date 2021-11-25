#!/bin/bash

version=$1
git pull origin master

# gradle build

gradle bootRepackage

nohup java -jar dist/LayIM-$version.jar >layim.log 2>&1 &

tail -f layim.log
