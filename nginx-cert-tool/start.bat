@echo off
chcp 65001 >nul
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.12
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0"
start javaw -jar target\nginx-cert-tool-1.0.0.jar
