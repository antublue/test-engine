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

PROJECT_ROOT_DIRECTORY=$(git rev-parse --show-toplevel)
cd "${PROJECT_ROOT_DIRECTORY}"
check_exit_code "Failed to change to project root directory"

grep -RiL "Copyright (C) .* The AntuBLUE test-engine project authors" api/src/main/java/
check_exit_code "Files are missing copyright"

grep -RiL "Copyright (C) .* The AntuBLUE test-engine project authors" engine/src/main/java/
check_exit_code "Files are missing copyright"

grep -RiL "Copyright (C) .* The AntuBLUE test-engine project authors" plugin/src/main/java/
check_exit_code "Files are missing copyright"
