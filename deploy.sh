#!/bin/bash

version=$1
git pull origin master

# gradle build

gradle bootRepackage

nohup java -jar dist/Sim-$version.jar > sim.log 2>&1 &

tail -f sim.log
