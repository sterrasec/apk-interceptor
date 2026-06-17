@echo off
setlocal
set SCHEME=
set APP_ID=
set OUTPUT_DIR=.\out

:parse
if "%~1"=="" goto validate
if "%~1"=="--scheme" set SCHEME=%~2& shift& shift& goto parse
if "%~1"=="--app-id" set APP_ID=%~2& shift& shift& goto parse
if "%~1"=="--output" set OUTPUT_DIR=%~2& shift& shift& goto parse
if "%~1"=="-h" goto usage
if "%~1"=="--help" goto usage
echo Unknown option: %~1
goto usage

:validate
if "%SCHEME%"=="" (
  echo Error: --scheme is required
  goto usage
)
if "%SCHEME%"=="intercept-poc-example" (
  echo Error: Replace the default dummy scheme with your authorized assessment scheme
  exit /b 1
)
powershell -NoProfile -Command "if ($env:SCHEME -notmatch '^[A-Za-z][A-Za-z0-9+.-]*$') { exit 1 }" || (
  echo Error: --scheme must be a valid URI scheme
  exit /b 1
)
if not "%APP_ID%"=="" (
  powershell -NoProfile -Command "if ($env:APP_ID -notmatch '^([A-Za-z][A-Za-z0-9_]*\.)+[A-Za-z][A-Za-z0-9_]*$') { exit 1 }"
  if errorlevel 1 (
    echo Error: --app-id must be a valid Android application ID
    exit /b 1
  )
)

echo Building apk-interceptor...
echo   Scheme: %SCHEME%
set GRADLE_ARGS=-PinterceptScheme=%SCHEME%
if not "%APP_ID%"=="" set GRADLE_ARGS=%GRADLE_ARGS% -PappId=%APP_ID%
call gradlew.bat assembleDebug %GRADLE_ARGS% || exit /b 1
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"
copy /Y app\build\outputs\apk\debug\app-debug.apk "%OUTPUT_DIR%\apk-interceptor-%SCHEME%-debug.apk" || exit /b 1
echo Build successful: %OUTPUT_DIR%\apk-interceptor-%SCHEME%-debug.apk
exit /b 0

:usage
echo Usage: build-interceptor.bat --scheme ^<custom_scheme^> [--app-id ^<application_id^>] [--output ^<directory^>]
exit /b 1
