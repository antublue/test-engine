#!/bin/bash

cd api
mvn clean package install
cd ..

cd core
mvn clean package install
cd ..

cd plugin
mvn clean package install
cd ..

cd examples
mvn clean verify
cd ..
