@echo off 

IF NOT EXIST "%CD%\pac.bat" (
 SET HOME=%~dp0
 ) ELSE (
 SET HOME=%CD%
 )

CHDIR /D "%HOME%"
start /D "%HOME%" pac.bat stop
