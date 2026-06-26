@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.11"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0"

echo Checking Java...
java -version
if errorlevel 1 (
    echo.
    echo Java failed. Install JDK 17+ and update JAVA_HOME in this file.
    pause
    exit /b 1
)

echo.
echo Starting HireSense AI (first run may take several minutes)...
call mvnw.cmd spring-boot:run
pause
