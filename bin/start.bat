@echo off

set AKKA_HOME=%~dp0..
set AKKA_CLASSPATH=%AKKA_HOME%\config;%AKKA_HOME%\lib\*
JAVA_OPTS="-Dakka.remote.netty.tcp.port=%2 -Xmx512m -Xms512m -Xmn2m "

java %JAVA_OPTS% -cp "%AKKA_CLASSPATH%" -Dakka.home="%AKKA_HOME%" akka.kernel.Main %1
