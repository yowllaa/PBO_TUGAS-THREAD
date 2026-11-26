@echo off
setlocal
if not exist out mkdir out
echo Compiling...
javac -cp "lib\*" -d out src\*.java
if errorlevel 1 (
    echo Compilation failed. Check JDK and Connector JAR.
    pause
    exit /b 1
)
echo Running To Do List Yola...
java -cp "out;lib\*" Main
pause
