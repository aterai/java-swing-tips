@echo off
setlocal
echo Swing Example
echo -------------------

if "%JAVA_HOME%" == "" goto :error
set MAIN_CLASS=example.MainPanel
set CLASSPATH=.\target\classes
for %%i in (.\lib\*.jar) do call :setpath %%i
goto :endsubs

:setpath
set CLASSPATH=%CLASSPATH%;%1
goto :EOF

:endsubs

echo Running with classpath "%CLASSPATH%"
echo Starting...
"%JAVA_HOME%\bin\java.exe" -classpath "%CLASSPATH%" %MAIN_CLASS% %*

goto :end

:error
echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

:end
