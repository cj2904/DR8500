IF NOT EXIST "%CD%\oistarter_1-1-2_0.jar" (
 SET HOME=%~dp0..
 ) ELSE (
 SET HOME=%CD%\..
 )

SET UI_PORT=28002
SET UI_HOST=localhost
SET UI_SERVICE=WBD002
start /D "%HOME%" javaw -Duser.language=en -Duser.home="%HOME%" -jar "%HOME%"/bin/oistarter_1-1-2_0.jar -host %UI_HOST% -port %UI_PORT% -zlpService %UI_SERVICE%.processor -zlpUser %USERNAME% 