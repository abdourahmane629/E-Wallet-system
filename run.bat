@echo off
chcp 65001 > nul
cls

echo ========================================
echo [INFO] Lancement E-Wallet Application
echo ========================================

set JAVA_HOME=C:\Program Files\Java\jdk-25
set PATH=%JAVA_HOME%\bin;%PATH%
set JAVAFX_HOME=C:\javafx-sdk-21.0.9

echo Java: %JAVA_HOME%
echo JavaFX: %JAVAFX_HOME%
echo.

set CLASSPATH=bin;lib/mysql-connector-j-8.0.33.jar;lib/poi-5.2.3.jar;lib/poi-ooxml-5.2.3.jar;lib/xmlbeans-5.1.1.jar;lib/commons-compress-1.25.0.jar;lib/commons-logging-1.2.jar;lib/pdfbox-2.0.29.jar;lib/fontbox-2.0.29.jar

echo [INFO] Chemin de classe: %CLASSPATH%
echo.

if not exist bin (
    echo [ERREUR] Dossier 'bin' non trouvé. Exécutez d'abord compile.bat
    pause
    exit /b 1
)

echo [INFO] Lancement de l'application...
java ^
    -cp "%CLASSPATH%" ^
    --module-path "%JAVAFX_HOME%/lib" ^
    --add-modules javafx.controls,javafx.fxml,javafx.web ^
    com.ewallet.gui.MainApp

echo.
echo [INFO] Application terminée.
pause