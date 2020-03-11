@echo on

IF NOT DEFINED PAC_HOME (
	SET PAC_HOME=%CD%\..
)
IF NOT DEFINED PAC_WORKING_DIR (
	SET PAC_WORKING_DIR=%CD%\..
)

SET PAC_ALL=pac-all_14-6-0_3
SET LIBS=%PAC_HOME%\lib
IF NOT EXIST "%LIBS%" SET LIBS=%CD%\..\..\lib

IF NOT EXIST "%PAC_WORKING_DIR%\log" mkdir "%PAC_WORKING_DIR%\log"
SET START_LOG_FILE=%PAC_WORKING_DIR%\log\pacstart.log

IF %1==start ( 
	echo Start ...
	goto start
) ELSE IF %1==stop (
	echo Stop ...
	goto stop
) ELSE IF %1==dump (
	echo Dump ...
	goto dump
) ELSE (
	echo "Unknown Command"
	goto end
)


:start
echo starting pac from directory %PAC_HOME%
echo starting pac from directory %PAC_HOME% > "%START_LOG_FILE%"
echo you can stop pac using Ctrl-C or from PAC Navigator UI.

echo "-----------------------------------" >> "%START_LOG_FILE%"
echo "Starting PAC script" >> "%START_LOG_FILE%"
echo " PAC_WORKING_DIR = %PAC_WORKING_DIR%" >> "%START_LOG_FILE%"
echo " PAC_HOME = %PAC_HOME%" >> "%START_LOG_FILE%"
echo "-----------------------------------" >> "%START_LOG_FILE%"

set CP=
set CP=%CP%;%PAC_HOME%\..\%PAC_ALL%\lib\*
set CP=%CP%;%PAC_HOME%\config\i18n
set CP=%CP%;%PAC_HOME%\config\views
set CP=%CP%;%LIBS%\esa\*
set CP=%CP%;%LIBS%\ext\*
set CP=%CP%;%PAC_HOME%\_classes
set CP=%CP%;%LIBS%\*

set PATH=%PATH%;%LIBS%
SET SYSTEM_PROPERTIES=-Ddeo.path=%DEO_PATH% -Dlocalcontext=%TRIGGER_CONTEXT% -Dde.znt.util.PropertyHelper.fileName="%PAC_HOME%"\config\properties\application.properties -Dfile.encoding=UTF-8 -Duser.language=en -Duser.home="%PAC_WORKING_DIR%" -DAppDir="%PAC_HOME%" -Drv.encoding=ISO8859_1 


echo "using classpath %CP%" >> "%START_LOG_FILE%"
echo "using path %PATH%" >> "%START_LOG_FILE%"
echo "using system properties %SYSTEM_PROPERTIES%" >> "%START_LOG_FILE%"

IF EXIST "%PAC_WORKING_DIR%/shutdown.cmd" rm "%PAC_WORKING_DIR%/shutdown.cmd"

java -verbose:gc -Xms32m -Xmx256m -classpath "%CP%" %SYSTEM_PROPERTIES% de.znt.pac.ProcessAutomationController >> "%START_LOG_FILE%" 2>&1
goto end

:dump
set /p ppid=<%PAC_WORKING_DIR%/application.pid
jstack %ppid% >> "%START_LOG_FILE%"
goto end

:stop
set CP=
set CP=%CP%;%LIBS%\*

SET UI_PORT=9890
SET UI_HOST=localhost

java -classpath "%CP%" de.znt.remote.shutdown.ShutdownRequestSender -host %UI_HOST% -port %UI_PORT% -timeout 30 -reason "Stop script."
goto end

:end
echo End script
exit