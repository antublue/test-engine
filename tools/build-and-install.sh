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

# Function to check exit code
function check_exit_code () {
  if [ ! $? -eq 0 ];
  then
    echo "------------------------------------------------------------------------"
    echo "${1}"
    echo "------------------------------------------------------------------------"
    exit 1
  fi
}

# Usage
if [ "$#" -ne 1 ];
then
  echo "Usage: ${0} <version>"
  exit 1
fi

VERSION="${1}"
PROJECT_ROOT_DIRECTORY=$(git rev-parse --show-toplevel)
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Check for any uncommitted changes
git diff --quiet HEAD
if [ ! $? -eq 0 ];
then
  echo "------------------------------------------------------------------------"
  echo "UNCOMMITTED CHANGES"
  echo "------------------------------------------------------------------------"
  echo ""
  git status
  exit 1
fi

cd "${PROJECT_ROOT_DIRECTORY}"
check_exit_code "Failed to change to project root directory"

# Verify the code builds
./mvnw clean verify
check_exit_code "Maven build failed"

# Delete any previous build branch
git branch -D "build-${VERSION}" > /dev/null 2>&1

# Checkout a build branch
git checkout -b "build-${VERSION}"
check_exit_code "Git checkout [${VERSION}] failed"

# Update the build versions
mvn versions:set -DnewVersion="${VERSION}" -DprocessAllModules
check_exit_code "Maven update versions [${VERSION}] failed"
rm -Rf $(find . -name "*versionsBackup")

# Build the version as a release
./mvnw -s ~/.m2/antublue.settings.xml -P clean install
check_exit_code "Maven deploy [${VERSION}] failed"

# Reset the branch
git reset --hard HEAD
check_exit_code "Git reset hard failed"

# Checkout the main branch
git checkout "${CURRENT_BRANCH}"
check_exit_code "Git checkout [${CURRENT_BRANCH} failed"

# Delete the build branch
git branch -D "build-${VERSION}"
check_exit_code "Git delete branch [build-${VERSION}] failed"

echo "------------------------------------------------------------------------"
echo "SUCCESS"
echo "------------------------------------------------------------------------"
