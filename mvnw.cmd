@ECHO OFF
SETLOCAL EnableDelayedExpansion

SET BASE_DIR=%~dp0
SET MAVEN_PROJECTBASEDIR=%BASE_DIR:~0,-1%
SET WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper
SET PROPS=%WRAPPER_DIR%\maven-wrapper.properties
SET JAR=%WRAPPER_DIR%\maven-wrapper.jar

IF NOT EXIST "%PROPS%" (
  ECHO Missing %PROPS%
  EXIT /B 1
)

IF NOT EXIST "%JAR%" (
  FOR /F "usebackq tokens=1,* delims==" %%A IN ("%PROPS%") DO (
    IF "%%A"=="wrapperUrl" SET WRAPPER_URL=%%B
  )

  IF "!WRAPPER_URL!"=="" (
    ECHO Missing wrapperUrl in %PROPS%
    EXIT /B 1
  )

  IF NOT EXIST "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
  ECHO Downloading Maven Wrapper from !WRAPPER_URL!
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$u='!WRAPPER_URL!';$o='%JAR%';(New-Object Net.WebClient).DownloadFile($u,$o)"
  IF ERRORLEVEL 1 EXIT /B 1
)

java -classpath "%JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
SET EXIT_CODE=%ERRORLEVEL%
ENDLOCAL & EXIT /B %EXIT_CODE%
