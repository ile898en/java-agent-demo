#!/bin/bash

# check if install the Maven
if ! mvn -v;
then
  echo "command mvn not found, Install the maven before executing the script！"
  exit 0
fi

# see ../dist/
mvn clean package  -Dmaven.test.skip

# tar
tar -zcvf simple-agent.tar.gz agent