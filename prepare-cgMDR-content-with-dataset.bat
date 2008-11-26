@echo off
call build.bat -f cancergrid\cgmdr-dist.xml prepare-cgmdr-content-with-dataset
call bin\client.bat -l -c /db/mdr -i -P cancergrid
call build.bat -f cancergrid\cgmdr-dist.xml copy-cgmdr-content-with-dataset