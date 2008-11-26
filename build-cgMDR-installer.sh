#!/bin/bash

./build.sh clean
./build.sh rebuild
./build.sh -f build/scripts/jarsigner.xml
./build.sh cgMDR-installer
