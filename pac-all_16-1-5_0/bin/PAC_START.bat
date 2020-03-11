@ECHO OFF

IF NOT EXIST "%CD%\pac.ps1" (
 SET HOME=%~dp0
 ) ELSE (
 SET HOME=%CD%
 )

PowerShell -ExecutionPolicy Unrestricted -File %HOME%\pac.ps1 start
