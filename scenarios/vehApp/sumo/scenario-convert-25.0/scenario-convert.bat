@ECHO OFF
SetLocal EnableDelayedExpansion

REM check if JAVA_HOME is set and define path to java binary accordingly (variable: java_bin)
if "%JAVA_HOME%"=="" (
  set java_bin=java
) else (
  set java_bin=%JAVA_HOME%\bin\java
)

set libs=

set Filter= .\tools\scenario-convert-*.jar
for %%f in (%Filter%) do (
if not "!libs!" == "" set libs=!libs!;
    set libs=!libs!%%f
)

"%java_bin%" -cp !libs!;lib\mosaic\*;lib\extended\*;lib\third-party\* com.dcaiti.mosaic.tools.scenarioconvert.core.Starter %*

EndLocal

exit /b %errorlevel%
