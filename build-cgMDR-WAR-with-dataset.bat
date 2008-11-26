@echo off
call build.bat clean
call build.bat rebuild
call build.bat -f build\scripts\jarsigner.xml
call build.bat -f cancergrid/cgmdr-dist.xml prepare-cgmdr-content-with-dataset
call build.bat -f cancergrid/cgmdr-dist.xml -Ddist.type=with-dataset dist-cgmdr-war