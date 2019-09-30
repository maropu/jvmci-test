#!/usr/bin/env bash

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# Shell script for running JMH benchmarks

set -e -o pipefail

# Determine the current working directory
_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# If needed, compile code
_JVMCI_TEST_VERSION=`grep "<version>" "${_DIR}/../pom.xml" | head -n1 | awk -F '[<>]' '{print $3}'`
# _BUILT_JAR="$_DIR/../assembly/jvmci-test_${_JVMCI_TEST_VERSION}-with-dependencies.jar"
_BUILT_JAR="$_DIR/../target/jvmci-test_${_JVMCI_TEST_VERSION}-with-dependencies.jar"
if [ ! -e $_BUILT_JAR ]; then
  cd ${_DIR}/.. && ./build/mvn clean package -DskipTests
fi

LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${_DIR}/../lib java -jar ${_BUILT_JAR}

