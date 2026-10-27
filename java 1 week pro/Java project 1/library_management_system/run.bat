@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
javac -Xlint:all -d out src\*.java
if errorlevel 1 (
  echo Compilation failed. Review the errors above.
  exit /b 1
)
java -cp out Main
endlocal
