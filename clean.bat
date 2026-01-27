@echo off
echo Nettoyage des fichiers compiles...
if exist bin rmdir /s /q bin
if exist run-simple.bat del run-simple.bat
if exist run-javafx.bat del run-javafx.bat
echo Fichiers nettoyes!
pause