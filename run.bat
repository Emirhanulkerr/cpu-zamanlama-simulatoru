@echo off
rem Projeyi Windows'ta derler ve calistirir.
rem   run.bat                 -> grafik arayuz
rem   run.bat --cli --ornek   -> konsol modu
setlocal
cd /d "%~dp0"

if not exist build\classes mkdir build\classes

javac --release 17 -encoding UTF-8 -d build\classes ^
  src\scheduler\*.java ^
  src\scheduler\model\*.java ^
  src\scheduler\algorithm\*.java ^
  src\scheduler\io\*.java ^
  src\scheduler\cli\*.java ^
  src\scheduler\ui\*.java
if errorlevel 1 exit /b 1

java -cp build\classes scheduler.Main %*
