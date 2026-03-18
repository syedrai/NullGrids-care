@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
java -jar target\medicare-secure-1.0.0.jar > app.log 2> app-error.log
