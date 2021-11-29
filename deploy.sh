#!/bin/bash

echo "if exists process, it will shut down"
ps -ef | grep -w mongod | grep -v grep | awk '{print $2}' | xargs kill -9
sleep 2

version=$1
git pull origin master

# ./gradlew build

./gradlew bootRepackage

nohup java -jar \
 -Dserver.port=8989 -Dspring.mail.username=568845948@qq.com \
 -Dspring.mail.password=aklbpawjeosybdfb \
 -Dspring.datasource.username=root \
 -Dspring.datasource.password=  \
 dist/Sim-$version.jar > sim.log 2>&1 &

tail -f sim.log
