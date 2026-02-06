@echo off
chcp 65001 > nul
cls

echo ========================================
echo [INFO] Compilation JavaFX E-Wallet
echo ========================================

REM ===== CONFIGURATION =====
set JAVA_HOME=C:\Program Files\Java\jdk-25
set JAVAFX_HOME=C:\javafx-sdk-21.0.9
set PATH=%JAVA_HOME%\bin;%PATH%

echo Java: %JAVA_HOME%
echo JavaFX: %JAVAFX_HOME%
echo.

REM ===== NETTOYAGE =====
if exist bin rmdir /s /q bin
mkdir bin

REM ===== LIBS =====
set LIBS=lib\mysql-connector-j-8.0.33.jar;lib\itextpdf-5.5.13.3.jar

REM ==================================================
REM 1️⃣ MODELS
REM ==================================================
echo [1/8] Compilation des models...
javac -d bin ^
    src/com/ewallet/core/models/*.java
if errorlevel 1 goto error

REM ==================================================
REM 2️⃣ UTILS (PDFExporter inclus)
REM ==================================================
echo [2/8] Compilation des utils...
javac -d bin ^
    -cp "bin;%LIBS%" ^
    src/com/ewallet/core/utils/*.java
if errorlevel 1 goto error

REM ==================================================
REM 3️⃣ DATABASE CONFIG
REM ==================================================
echo [3/8] Compilation DatabaseConfig...
javac -d bin ^
    -cp "bin;%LIBS%" ^
    src/com/ewallet/core/DatabaseConfig.java
if errorlevel 1 goto error

REM ==================================================
REM 4️⃣ DAO
REM ==================================================
echo [4/8] Compilation des DAO...
javac -d bin ^
    -cp "bin;%LIBS%" ^
    src/com/ewallet/core/dao/*.java
if errorlevel 1 goto error

REM ==================================================
REM 5️⃣ SERVICES
REM ==================================================
echo [5/8] Compilation des services...
javac -d bin ^
    -cp "bin;%LIBS%" ^
    src/com/ewallet/core/services/*.java
if errorlevel 1 goto error

REM ==================================================
REM 6️⃣ MAIN APP (JavaFX)
REM ==================================================
echo [6/8] Compilation MainApp...
javac -d bin ^
    --module-path "%JAVAFX_HOME%\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    -cp "bin;%LIBS%" ^
    src/com/ewallet/gui/MainApp.java
if errorlevel 1 goto error

REM ==================================================
REM 7️⃣ CONTROLLERS (JavaFX)
REM ==================================================
echo [7/8] Compilation des controllers...
javac -d bin ^
    --module-path "%JAVAFX_HOME%\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    -cp "bin;%LIBS%" ^
    src/com/ewallet/gui/controllers/*.java
if errorlevel 1 goto error

REM ==================================================
REM 8️⃣ FIN
REM ==================================================
echo.
echo ✅ COMPILATION TERMINÉE AVEC SUCCÈS
echo.
pause
exit /b 0

:error
echo.
echo ❌ ERREUR DE COMPILATION
echo Consulte le message ci-dessus pour le détail.
pause
exit /b 1  