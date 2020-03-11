@echo off
REM ----------------------------------------------------------------------------------------
REM Install all JARs that are not available at public Maven repositories to local repository
REM ----------------------------------------------------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
for %%p in (..\pom\parent\*.pom) do (
    set pom_file=%%p
    mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=!pom_file! -DpomFile=!pom_file!
)
for %%p in (..\pom\*.pom) do (
    set pom_file=%%p
    set artifact_id=%%~np
    set jar_file=..\..\lib\!artifact_id!.jar
    set javadoc_file=..\javadoc\!artifact_id!-javadoc.jar
    if exist !javadoc_file! (
        mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=!jar_file! -Djavadoc=!javadoc_file! -DpomFile=!pom_file!
    ) else (
        mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=!jar_file! -DpomFile=!pom_file!
    )
)
pause
