@echo off
call build.bat clean
call build.bat rebuild
call build.bat -f build\scripts\jarsigner.xml
call build.bat -f cancergrid/cgmdr-dist.xml prepare-cgmdr-content
call build.bat -f cancergrid/cgmdr-dist.xml -Ddist.type=default dist-cgmdr-war