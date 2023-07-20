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
  if [ ! $? ];
  then
    echo "${1}"
    exit 1
  fi
}

function emit_error () {
  echo "${1}"
  exit 1;
}

if [ "$#" -ne 1 ];
then
  echo "Usage: ${0} <version>"
  exit 1
fi

VERSION="${1}"
CURRENT_DIRECTORY=${PWD}
CURRENT_DIRECTORY=${CURRENT_DIRECTORY:-/}

git checkout -b "release-${VERSION}"
check_exit_code "Git checkout [${VERSION}] failed"

mvn versions:set -DnewVersion="${VERSION}" -DprocessAllModules
check_exit_code "Maven update versions [${VERSION}] failed"
rm -Rf "$(find . -name "*versionsBackup")"

cd "${CURRENT_DIRECTORY}" || emit_error "Failed to change directory [${CURRENT_DIRECTORY}]"
./mvnw clean verify
check_exit_code "Maven build [${VERSION}] failed"

git add -u
check_exit_code "Git add failed"

git commit -m "${VERSION}"
check_exit_code "Git commit failed"

./mvnw -s ~/.m2/antublue.settings.xml -P release clean deploy
check_exit_code "Maven deploy [${VERSION}] failed"

git push --set-upstream origin release-"${VERSION}"
check_exit_code "Git push [${VERSION}] failed"

git tag "${VERSION}"
check_exit_code "Git tag [${VERSION}] failed"

git push origin "${VERSION}"
check_exit_code "Git tag [${VERSION}] push failed"

git checkout main
check_exit_code "Git checkout [main] failed"

