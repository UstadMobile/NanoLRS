#!/bin/bash

BASEDIR=$(pwd)

J2OBJC_HOME=~/local/j2objc/
CHECKOUT_DIR=lib/checkout

# Input Java source
NANOLRS_CORE_SRCDIR_MAIN=../nanolrs-core/src/main/java
NANOLRS_CORE_SRCDIR_TEST=../nanolrs-core/src/test/java

NANOLRS_CORE_SRCFILES_MAIN=$(find $NANOLRS_CORE_SRCDIR_MAIN -name "*.java")
NANOLRS_CORE_SRCFILES_TEST=$(find $NANOLRS_CORE_SRCDIR_TEST -name "*.java")

# NanoHTTPD library to use
NANOHTTPD_GIT_URL="https://github.com/NanoHttpd/nanohttpd.git"
NANOHTTPD_RELEASE_TAG="nanohttpd-project-2.3.1"

if [ ! -e $CHECKOUT_DIR ]; then
    mkdir -p $CHECKOUT_DIR
fi

# Get NanoHTTP library
cd $CHECKOUT_DIR
if [ ! -e nanohttpd ]; then
    git clone $NANOHTTPD_GIT_URL nanohttpd
    cd nanohttpd
    git checkout tags/$NANOHTTPD_RELEASE_TAG
else
    cd nanohttpd
    git checkout master
    git pull
    git checkout tags/$NANOHTTPD_RELEASE_TAG
fi

cd $BASEDIR

NANOHTTPD_CORE_SRCDIR_MAIN=$CHECKOUT_DIR/nanohttpd/core/src/main/java
NANOHTTPD_NANOLETS_SRCDIR_MAIN=$CHECKOUT_DIR/nanohttpd/nanolets/src/main/java

NANOHTTPD_CORE_SRCFILES_MAIN=$(find $NANOHTTPD_CORE_SRCDIR_MAIN -name "*.java")
NANOHTTPD_NANOLETS_SRCFILES_MAIN=$(find $NANOHTTPD_NANOLETS_SRCDIR_MAIN -name "*.java")

if [ ! -e Generated ]; then
    mkdir Generated
fi

# Translate the NanoHTTPD files and put them in a directory
if [ -e Generated/NanoHttpd ]; then
    rm Generated/NanoHttpd/*.h Generated/NanoHttpd/*.m
fi

$J2OBJC_HOME/j2objc -d Generated/NanoHttpd/ \
   -sourcepath $NANOHTTPD_CORE_SRCDIR_MAIN:$NANOHTTPD_NANOLETS_SRCDIR_MAIN \
   --no-package-directories \
   $NANOHTTPD_CORE_SRCFILES_MAIN $NANOHTTPD_NANOLETS_SRCFILES_MAIN

# Translate the NanoLrs core
if [ -e Generated/NanoLrs-Main ]; then
   rm Generated/NanoLrs-Main/*.h Generated/NanoLrs-Main/*.m
fi

$J2OBJC_HOME/j2objc -d Generated/NanoLrs-Main/ \
   -sourcepath $NANOHTTPD_CORE_SRCDIR_MAIN:$NANOHTTPD_NANOLETS_SRCDIR_MAIN:$NANOLRS_CORE_SRCDIR_MAIN \
   --no-package-directories $NANOLRS_CORE_SRCFILES_MAIN

# Translate the NanoLrs tests
if [ -e Generated/NanoLrs-Test ]; then
    rm Generated/NanoLrs-Test/*.h Generated/NanoLrs-Test/*.m
fi

$J2OBJC_HOME/j2objc -d Generated/NanoLrs-Test \
   -classpath $J2OBJC_HOME/lib/j2objc_junit.jar \
   -sourcepath $NANOHTTPD_CORE_SRCDIR_MAIN:$NANOHTTPD_NANOLETS_SRCDIR_MAIN:$NANOLRS_CORE_SRCDIR_MAIN:$NANOLRS_CORE_SRCDIR_TEST \
   --no-package-directories $NANOLRS_CORE_SRCFILES_TEST


