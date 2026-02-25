@REM ----------------------------------------------------------------------------
@REM Maven Wrapper batch script for Windows
@REM ----------------------------------------------------------------------------
@REM This script downloads and runs the Maven Wrapper.
@REM It allows running Maven without having Maven installed globally.
@REM
@REM Usage: mvnw.cmd clean install
@REM ----------------------------------------------------------------------------

@echo off

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar
set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@REM Download maven-wrapper.jar if it doesn't exist
if not exist "%MAVEN_WRAPPER_JAR%" (
    echo Downloading Maven Wrapper...
    powershell -Command "(New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%MAVEN_WRAPPER_JAR%')"
)

@REM Run Maven
"%JAVA_HOME%\bin\java.exe" ^
  -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
  -Dwrapper.jar="%MAVEN_WRAPPER_JAR%" ^
  -jar "%MAVEN_WRAPPER_JAR%" %*
