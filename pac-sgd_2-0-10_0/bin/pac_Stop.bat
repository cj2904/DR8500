@echo off 

IF NOT EXIST "%CD%\pac.bat" (
 SET HOME=%~dp0
 ) ELSE (
 SET HOME=%CD%
 )

CHDIR /D "%HOME%"
SET TRIGGER_CONTEXT=
SET DEO_PATH=
start /D "%HOME%" pac.bat stop
