@echo on 

IF NOT EXIST "%CD%\pac.bat" (
 SET HOME=%~dp0
 ) ELSE (
 SET HOME=%CD%
 )

echo "%HOME%"
CHDIR /D "%HOME%"
SET TRIGGER_CONTEXT=WinApiCharacterizationTrigger.xml
SET DEO_PATH=../config/win-api-characterization-deo
start /D "%HOME%" pac.bat start
