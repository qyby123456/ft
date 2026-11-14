@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
echo ============================================
echo  QYS Bosses build (Forge 1.20.1)
echo ============================================

rem ---- 1. locate JDK 17/21 ----
set "JAVA_HOME_USE="
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\javac.exe" set "JAVA_HOME_USE=%JAVA_HOME%"
)
if not defined JAVA_HOME_USE (
  if exist "%APPDATA%\.minecraft\runtime\java-runtime-gamma-snapshot\bin\javac.exe" set "JAVA_HOME_USE=%APPDATA%\.minecraft\runtime\java-runtime-gamma-snapshot"
)
if not defined JAVA_HOME_USE (
  for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-17*" "C:\Program Files\Java\jdk-17*" "C:\Program Files\Microsoft\jdk-17*") do (
    if not defined JAVA_HOME_USE if exist "%%d\bin\javac.exe" set "JAVA_HOME_USE=%%d"
  )
)
if not defined JAVA_HOME_USE (
  echo [ERROR] JDK 17 not found. Set JAVA_HOME or install Temurin 17.
  exit /b 1
)
echo [INFO] JDK: %JAVA_HOME_USE%

rem ---- 2. locate cached gradle 8.8 ----
set "GRADLE_BAT="
for /d %%d in ("%USERPROFILE%\.gradle\wrapper\dists\gradle-8.8-bin\*") do (
  if not defined GRADLE_BAT if exist "%%d\gradle-8.8\bin\gradle.bat" set "GRADLE_BAT=%%d\gradle-8.8\bin\gradle.bat"
)
if not defined GRADLE_BAT (
  echo [ERROR] gradle-8.8 cache not found. Install Gradle 8.8 first.
  exit /b 1
)
echo [INFO] Gradle: %GRADLE_BAT%

set "JAVA_HOME=%JAVA_HOME_USE%"
echo [INFO] building (first run downloads ForgeGradle deps, may take long)...
call "%GRADLE_BAT%" %* build --console=plain --no-daemon
if errorlevel 1 (
  echo [ERROR] build failed.
  exit /b 1
)
echo [OK] artifact: %~dp0build\libs\qys-bosses.jar
endlocal
