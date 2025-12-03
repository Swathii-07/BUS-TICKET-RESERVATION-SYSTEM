@echo off
REM ==== Bus Reservation App Setup, Compile & Run ====

cd /d "%~dp0"

REM ==== Step 1: Check if MySQL exists ====
echo ===============================================
echo 🔹 Checking MySQL connection...
echo ===============================================
mysql -u root -proot -e "SELECT VERSION();" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ MySQL not found or credentials incorrect!
    pause
    exit /b
) else (
    echo ✅ MySQL connection successful!
)

REM ==== Step 2: Check if database exists ====
echo ===============================================
echo 🔹 Checking if database 'bus_reservation' exists...
echo ===============================================
mysql -u root -proot -e "USE bus_reservation;" 2>nul
if %errorlevel% neq 0 (
    echo 🔹 Database not found, creating and setting up...
    if not exist setup.sql (
        echo ❌ setup.sql not found in %CD%!
        pause
        exit /b
    )
    mysql -u root -proot < setup.sql
    if %errorlevel% neq 0 (
        echo ❌ Database setup failed!
        pause
        exit /b
    ) else (
        echo ✅ Database setup completed!
    )
) else (
    echo ✅ Database already exists, skipping setup...
)

REM ==== Step 3: Check Java files and MySQL connector ====
echo ===============================================
echo 🔹 Checking Java files and MySQL Connector...
echo ===============================================
if not exist lib\mysql-connector-j-9.4.0.jar (
    echo ❌ MySQL Connector not found in lib folder!
    pause
    exit /b
) else (
    echo ✅ MySQL Connector found.
)

if not exist src\BusReservationApp.java (
    echo ❌ BusReservationApp.java not found in src folder!
    pause
    exit /b
) else (
    echo ✅ Java source file found.
)

REM ==== Step 4: Compile Java Program ====
echo ===============================================
echo 🔹 Compiling BusReservationApp.java...
echo ===============================================
javac -cp "lib\mysql-connector-j-9.4.0.jar;." src\BusReservationApp.java
if %errorlevel% neq 0 (
    echo ❌ Compilation failed!
    pause
    exit /b
) else (
    echo ✅ Compilation successful!
)

REM ==== Step 5: Run Java Program ====
echo ===============================================
echo 🔹 Running BusReservationApp...
echo ===============================================
java -cp "lib\mysql-connector-j-9.4.0.jar;src" BusReservationApp
if %errorlevel% neq 0 (
    echo ❌ Program execution failed!
) else (
    echo ✅ Program executed successfully!
)

echo ===============================================
echo 🏁 Program finished.
echo ===============================================
pause
