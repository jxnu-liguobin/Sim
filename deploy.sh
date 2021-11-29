#!/bin/bash

version=$1
git pull origin master

# ./gradlew build

./gradlew bootRepackage

nohup java -jar -Dspring.mail.username=568845948@qq.com \
 -Dspring.mail.password=aklbpawjeosybdfb \
 -Dspring.datasource.username=root \
 -Dspring.datasource.password=  \
 dist/Sim-$$version.jar > sim.log 2>&1 &

tail -f sim.log
