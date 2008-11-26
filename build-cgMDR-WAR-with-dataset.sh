#!/bin/bash

./build.sh clean-all
./build.sh rebuild
./build.sh -f build/scripts/jarsigner.xml
./build.sh -f cancergrid/cgmdr-dist.xml prepare-cgmdr-content-with-dataset
./build.sh -f cancergrid/cgmdr-dist.xml -Ddist.type=with-dataset dist-cgmdr-war
