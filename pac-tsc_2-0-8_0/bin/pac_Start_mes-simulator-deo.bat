@echo on 

IF NOT EXIST "%CD%\pac.bat" (
 SET HOME=%~dp0
 ) ELSE (
 SET HOME=%CD%
 )

echo "%HOME%"
CHDIR /D "%HOME%"
SET TRIGGER_CONTEXT=MesSimulatorTriggers.xml
SET TRIGGER_DEFAULT_CONTEXT=classpath:/sg/znt/pac/resources/xml/TscPacCoreContext.xml
SET DEO_PATH=../config/mes-simulator-deo
start /D "%HOME%" pac.bat start
