@echo off
REM Script de compilation et creation du JAR executable (Windows)

echo Compilation du projet Trio...

REM Creer les repertoires de sortie
if not exist build\classes mkdir build\classes
if not exist build\jar mkdir build\jar

REM Compiler les sources
javac -d build\classes -sourcepath src src\model\*.java src\game\*.java src\ui\*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo Compilation reussie!
    
    REM Creer le JAR
    cd build\classes
    jar cvfe ..\jar\Trio.jar ui.FenetreConnexion . >nul 2>&1
    cd ..\..
    
    echo JAR cree: build\jar\Trio.jar
    echo.
    echo Pour lancer le jeu graphique:
    echo   java -jar build\jar\Trio.jar
    echo.
    echo Pour lancer le jeu en mode console:
    echo   java -cp build\classes ui.TrioConsole
) else (
    echo.
    echo Erreur de compilation!
    exit /b 1
)
