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

GIT_ROOT_DIRECTORY=$(git rev-parse --show-toplevel)
cd "${GIT_ROOT_DIRECTORY}"

echo "Checking Java files in [api] for copyright"
grep -RiL "Copyright (C)" api/src/main/java/

echo "Checking Java files in [engine] for copyright"
grep -RiL "Copyright (C)" engine/src/main/java/

echo "Checking Java files in [plugin] for copyright"
grep -RiL "Copyright (C)" plugin/src/main/java/