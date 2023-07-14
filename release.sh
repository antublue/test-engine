#!/bin/bash

#
# Copyright (C) 2023 The AntuBLUE test-engine project authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

function check_exit_code () {
  if [ ! $? -eq 0 ];
  then
    echo "${1}"
    exit 1
  fi
}

if [ "$#" -ne 1 ];
then
  echo "Usage: ${0} <version>"
  exit 1
fi

RELEASE="${1}"
CURRENT_DIRECTORY=${PWD}
CURRENT_DIRECTORY=${CURRENT_DIRECTORY:-/}

git checkout -b "release-${RELEASE}"
check_exit_code "Git checkout [${RELEASE}] failed"

mvn versions:set -DnewVersion="${RELEASE}"
check_exit_code "Maven update [parent] versions failed"
rm -Rf `find . -name "*versionsBackup"`

cd ${CURRENT_DIRECTORY}/api
mvn versions:set -DnewVersion="${RELEASE}"
check_exit_code "Maven update [api] versions failed"
rm -Rf `find . -name *versionsBackup`

cd ${CURRENT_DIRECTORY}/engine
mvn versions:set -DnewVersion="${RELEASE}"
check_exit_code "Maven update [engine] versions failed"
rm -Rf `find . -name *versionsBackup`

cd ${CURRENT_DIRECTORY}/plugin
mvn versions:set -DnewVersion="${RELEASE}"
check_exit_code "Maven update [plugin] versions failed"
rm -Rf `find . -name *versionsBackup`

cd ${CURRENT_DIRECTORY}/examples
mvn versions:set -DnewVersion="${RELEASE}"
check_exit_code "Maven update [examples] versions"
rm -Rf `find . -name *versionsBackup`

cd ${CURRENT_DIRECTORY}
./mvnw clean verify
check_exit_code "Maven build failed"

git add -u
check_exit_code "Git add failed"

git commit -m "${RELEASE}"
check_exit_code "Git commit failed"

./mvnw -s ~/.m2/antublue.settings.xml -P release clean deploy
check_exit_code "Maven deploy [${RELEASE}] failed"

git tag "${RELEASE}"
check_exit_code "Git tag [${RELEASE}] failed"

git checkout main
check_exit_code "Git checkout [main] failed"

