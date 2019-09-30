#!/usr/bin/env bash

set -e -o pipefail

# Determines the current working directory
_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Loads versions from `pom.xml`
LLVM_VERSION=`grep "<llvm.version>" "${_DIR}/../../pom.xml" | head -n1 | awk -F '[<>]' '{print $3}'`

# Downloads any application tarball given a URL, the expected tarball name,
# and, optionally, a checkable binary path to determine if the binary has
# already been installed
## Arg1 - URL
## Arg2 - Tarball Name
## Arg3 - Checkable Binary
download_app() {
  local remote_tarball="$1/$2"
  local local_tarball="${_DIR}/$2"
  local binary="${_DIR}/$3"

  # setup `curl` and `wget` silent options if we're running on Jenkins
  local curl_opts="-L"
  local wget_opts=""
  if [ -n "$AMPLAB_JENKINS" ]; then
    curl_opts="-s ${curl_opts}"
    wget_opts="--quiet ${wget_opts}"
  else
    curl_opts="--progress-bar ${curl_opts}"
    wget_opts="--progress=bar:force ${wget_opts}"
  fi

  if [ -z "$3" -o ! -f "$binary" ]; then
    # check if we already have the tarball
    # check if we have curl installed
    # download application
    [ ! -f "${local_tarball}" ] && [ $(command -v curl) ] && \
      echo "exec: curl ${curl_opts} ${remote_tarball}" 1>&2 && \
      curl ${curl_opts} "${remote_tarball}" > "${local_tarball}"
    # if the file still doesn't exist, lets try `wget` and cross our fingers
    [ ! -f "${local_tarball}" ] && [ $(command -v wget) ] && \
      echo "exec: wget ${wget_opts} ${remote_tarball}" 1>&2 && \
      wget ${wget_opts} -O "${local_tarball}" "${remote_tarball}"
    # if both were unsuccessful, exit
    [ ! -f "${local_tarball}" ] && \
      echo -n "ERROR: Cannot download $2 with cURL or wget; " && \
      echo "please download manually and try again." && \
      exit 2
    cd "${_DIR}" && tar -xvf "$2"
    rm -rf "$local_tarball"
  fi
}

install_llvm_from_source() {
  download_app \
    "http://releases.llvm.org/${LLVM_VERSION}" \
    "llvm-${LLVM_VERSION}.src.tar.xz" \
    "llvm-${LLVM_VERSION}.src/configure"

  # On Amazon Linux AMI ami-01e24be29428c15b2 (not Amazon Linux 2 AMI), you need to run lines below
  # before running this script:
  #
  # // Installs needed packages first
  # $ sudo yum install -y gcc48-c++ clang cmake python libarchive-devel curl-devel expat-devel zlib-devel xz-devel
  #
  # // LLVM requires gcc-c++ v4.8.0+, python v2.7+, and zlib v1.2.3.4+
  # // https://releases.llvm.org/7.0.1/docs/GettingStarted.html#software
  # $ g++ -v
  # Target: x86_64-amazon-linux
  # Thread model: posix
  # gcc version 4.8.5 20150623 (Red Hat 4.8.5-28) (GCC)
  #
  # // Compiles and installs jsoncpp
  # $ wget https://github.com/open-source-parsers/jsoncpp/archive/1.7.5.tar.gz
  # $ tar zxvf 1.7.5.tar.gz
  # $ cd jsoncpp-1.7.5
  # $ mkdir build
  # $ cd build
  # $ cmake ..
  # $ make
  # $ sudo make install
  #
  # // Compiles and installs newer cmake (3.4.3 or higher)
  # $ wget https://cmake.org/files/v3.6/cmake-3.6.1.tar.gz
  # $ tar zxvf cmake-3.6.1.tar.gz
  # $ cd cmake-3.6.1
  # $ ./bootstrap --prefix=/usr --system-libs --mandir=/share/man --docdir=/share/doc/cmake-3.6.1
  # $ make
  # $ sudo make install
  local src_dir=${_DIR}/"llvm-${LLVM_VERSION}.src"
  local build_dir=${src_dir}/build
  local binary=${build_dir}/bin/llvm-config

  [ ! -f "${binary}" ] && [ $(command -v cmake) ] && \
    echo "exec: cmake" 1>&2 && mkdir ${build_dir} && cd ${build_dir} && \
    cmake -G 'Unix Makefiles' -DCMAKE_BUILD_TYPE=Release -DLLVM_BUILD_TEST=OFF .. && make -j4 check-all && \
    cd ${_DIR}

  # Checks if the compilation finished successfully
  [ ! -f "${binary}" ] && \
    echo -n "ERROR: LLVM compilation failed; please check failure reasons." && \
    exit 2

  LLVM_DIR=${build_dir}
}

# Installs LLVM
install_llvm_from_source

# Builds a native library for the current platform:
CXX=clang++ LLVM_DIR=${LLVM_DIR} ${_DIR}/waf configure
${_DIR}/waf -v

