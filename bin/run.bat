@echo off

rem $Id: run.bat 6960 2007-11-21 08:11:45Z dizzzz $

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo Java environment not found. Please set
echo your JAVA_HOME environment variable to
echo the home of your JDK.
goto :eof

:gotJavaHome
if not "%EXIST_HOME%" == "" goto gotExistHome

rem try to guess
set EXIST_HOME=.
if exist %EXIST_HOME%\start.jar goto gotExistHome
set EXIST_HOME=..
if exist %EXIST_HOME%\start.jar goto gotExistHome

echo EXIST_HOME not found. Please set your
echo EXIST_HOME environment variable to the
echo home directory of eXist.
goto :eof

:gotExistHome
set JAVA_ENDORSED_DIRS="%EXIST_HOME%"\lib\endorsed
set JAVA_OPTS="-Xms16000k -Xmx256000k -Dfile.encoding=UTF-8 -Djava.endorsed.dirs=%JAVA_ENDORSED_DIRS%"

:gotJavaOpts
"%JAVA_HOME%\bin\java" "%JAVA_OPTS%"  -Dexist.home="%EXIST_HOME%" -jar "%EXIST_HOME%\start.jar" %1 %2 %3 %4 %5 %6 %7 %8 %9
:eof