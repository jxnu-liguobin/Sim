#!/bin/bash

git pull origin master

gradle build

gradle bootRepackage

nohup java -jar dist/LayIM-1.2.1.jar >layim.log 2>&1 &

tail -f layim.log
