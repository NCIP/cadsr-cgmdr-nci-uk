@echo off

rem $Id: shutdown.bat 7746 2008-05-10 21:24:55Z wolfgang_m $

if not "%JAVA_HOME%" == "" goto gotJavaHome

rem will be set by the installer
set JAVA_HOME=$JDKPath

:gotJavaHome
if not "%EXIST_HOME%" == "" goto gotExistHome

rem will be set by the installer
set EXIST_HOME=$INSTALL_PATH

:gotExistHome
"%JAVA_HOME%\bin\java" -Dexist.home="%EXIST_HOME%" -jar "%EXIST_HOME%\start.jar" shutdown %1 %2 %3 %4 %5 %6 %7 %8 %9
:eof
