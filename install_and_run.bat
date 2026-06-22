@echo off
setlocal
rem Define absolute path to ADB
set ADB="C:\Users\neopradeepl\AppData\Local\Android\Sdk\platform-tools\adb.exe"

rem Build the debug APK
call gradlew assembleDebug
if %errorlevel% neq 0 (
  echo Build failed with error %errorlevel%
  exit /b %errorlevel%
)
rem Install the APK on the attached device
%ADB% -s VSTWWWLFFE856LXS install -r app\build\outputs\apk\debug\app-debug.apk
if %errorlevel% neq 0 (
  echo Install failed with error %errorlevel%
  exit /b %errorlevel%
)
rem Launch the app
%ADB% -s VSTWWWLFFE856LXS shell am start -n com.prabs.ceipts/.MainActivity
if %errorlevel% neq 0 (
  echo Launch failed with error %errorlevel%
  exit /b %errorlevel%
)
echo Installation and launch completed successfully.
endlocal

