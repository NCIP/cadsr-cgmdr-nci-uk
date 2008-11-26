@echo off
call build.bat clean
call build.bat rebuild
call build.bat -f build\scripts\jarsigner.xml
call build.bat cgMDR-installer