#!/bin/bash
# Script de compilation et creation du JAR executable

echo "Compilation du projet Trio..."

# Creer le repertoire de sortie
mkdir -p build/classes
mkdir -p build/jar

# Compiler les sources
javac -d build/classes -sourcepath src src/model/*.java src/game/*.java src/ui/*.java

if [ $? -eq 0 ]; then
    echo "Compilation reussie!"
    
    # Creer le JAR
    cd build/classes
    jar cvfe ../jar/Trio.jar ui.FenetreConnexion . > /dev/null 2>&1
    cd ../../
    
    echo "JAR cree: build/jar/Trio.jar"
    echo ""
    echo "Pour lancer le jeu graphique:"
    echo "  java -jar build/jar/Trio.jar"
    echo ""
    echo "Pour lancer le jeu en mode console:"
    echo "  java -cp build/classes ui.TrioConsole"
else
    echo "Erreur de compilation!"
    exit 1
fi
