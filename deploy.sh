#!/bin/bash

version=$1
git pull origin master

# ./gradlew build

./gradlew bootRepackage

nohup java -jar dist/Sim-$version.jar > sim.log 2>&1 &

tail -f sim.log
