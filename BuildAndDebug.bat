@ECHO OFF
set package="com.xayup.multipad"
set activity=".MainActivity"
set build="sucess"

rem To compile
call gradlew assemble

if %errorlevel%==1 goto :exit

rem Check if a device is connected. If there is it will go straight to
rem :install, otherwise the loop ends and goes to the next line

for /f "tokens=*" %%a in ('adb devices ^| findstr /X /R .*device') do goto :install

:connect_device
	echo Trying to connect a device
	for /f "tokens=*" %%a in ('adb reconnect ^| findstr "no devices"') do goto :no_device
	timeout /t 1 /nobreak

rem Install, launch and debug the application on the device.
:install
	adb install -r ./app/build/outputs/apk/debug/app-debug.apk
	adb shell "am start -a android.intent.action.MAIN -n %package%/%activity%"
	set pid=""
	goto :get_pid_loop

rem This is a makeshift loop where it tries to set the "pid" variable
rem if %package% has been started and it will check this constantly
rem until the user terminates the process.
:get_pid_loop
	for /f "tokens=*" %%a in ('adb shell pidof -s %package%') do set pid=%%a
	if %pid%=="" goto :get_pid_loop
	goto :start_logcat

:start_logcat
	cls
	adb logcat -C --pid=%pid%
	goto :exit

:no_device
	echo no devices/emulators found. Press any key to try again...
	pause >null
	goto :connect_device

:exit