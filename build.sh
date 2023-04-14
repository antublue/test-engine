#!/bin/bash

cd api || exit 1
mvn clean package install || exit 1
cd .. || exit 1

cd core || exit 1
mvn clean package install || exit 1
cd ..  || exit 1

cd plugin || exit 1
mvn clean package install || exit 1
cd .. || exit 1

cd examples || exit 1
mvn clean verify || exit 1
cd ..  || exit 1
