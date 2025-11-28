# Projet AP4B - Jeu Trio en Java

## 📋 Description du Projet

Adaptation numérique du jeu de cartes **Trio** dans un contexte UTBM (Université de Technologie de Belfort-Montbéliard). 

Le projet suit une progression sur 3 séances:
1. **Séance 1**: Conception UML et modélisation ✓
2. **Séance 2**: Implémentation du cœur logique (mode console) ✓
3. **Séance 3**: Interface graphique Swing ✓

## 🎮 Règles du Jeu Trio

### Objectif
Trouver le maximum de **trios** de cartes valides parmi les 12 cartes en jeu.

### Structure des Cartes
Chaque carte possède 4 attributs:
- **Valeur**: 1, 2 ou 3 (nombre de symboles)
- **Forme**: Cercle (●), Carré (■), Ondulation (〰)
- **Couleur**: Rouge 🔴, Vert 🟢, Violet 🟣
- **Remplissage**: Plein, Vide, Rayé

**Total**: 81 cartes (3 × 3 × 3 × 3)

### Validation d'un Trio
Pour que 3 cartes forment un **trio valide**, pour chaque attribut:
- Les 3 valeurs doivent être **identiques** OU
- Les 3 valeurs doivent être **toutes différentes**

## 📁 Structure du Projet Java

```
trio/
├── src/
│   ├── model/                    # Modèle de données
│   │   ├── Carte.java           # Classe représentant une carte
│   │   ├── Joueur.java          # Classe représentant un joueur
│   │   ├── Deck.java            # Gestion du paquet de cartes (81)
│   │   ├── Couleur.java         # Énumération des couleurs
│   │   ├── Forme.java           # Énumération des formes
│   │   └── Remplissage.java     # Énumération des remplissages
│   │
│   ├── game/                     # Logique du jeu
│   │   └── Trio.java            # Classe principale du jeu
│   │
│   └── ui/                       # Interface utilisateur
│       ├── TrioConsole.java     # Interface console (Séance 2)
│       └── TrioGUI.java         # Interface Swing (Séance 3)
│
├── build.sh / build.bat          # Scripts de compilation
├── README.md                      # Ce fichier
├── package.json                  # Informations projet
└── Sujet Projet AP4B A25.pdf     # Énoncé du projet
```

## 🏗️ Architecture et Design Patterns

### Patterns Utilisés

1. **MVC (Model-View-Controller)**
   - Model: `model/*` + `game/Trio.java`
   - View & Controller: `ui/TrioConsole.java` et `ui/TrioGUI.java`

2. **Enumeration Pattern**: Pour Forme, Couleur, Remplissage

3. **Factory Pattern**: Génération automatique des 81 cartes dans `Deck.java`

## 🚀 Installation et Utilisation

### Prérequis
- Java 11 ou supérieur

### Compilation

**Linux/Mac:**
```bash
chmod +x build.sh
./build.sh
```

**Windows:**
```cmd
build.bat
```

### Exécution

**Mode GUI (Graphique - Swing):**
```bash
java -jar build/jar/Trio.jar
```

**Mode Console (Texte - Séance 2):**
```bash
java -cp build/classes ui.TrioConsole
```

## 📊 Fonctionnalités Implémentées

### Séance 1 - Conception ✓
- [x] Diagramme de cas d'utilisation UML
- [x] Diagramme de classes UML complet
- [x] Description des scénarios de jeu
- [x] Structure de base en Java avec packages

### Séance 2 - Logique du Jeu ✓
- [x] Génération des 81 cartes (Deck)
- [x] Validation des trios selon les règles
- [x] Gestion des tours et joueurs
- [x] Gestion du score
- [x] Interface console pour tests
- [x] Logique complète et testée

### Séance 3 - Interface Graphique ✓
- [x] Interface Swing complète et fonctionnelle
- [x] Affichage graphique des cartes en grille 4×3
- [x] Sélection interactive (clic souris)
- [x] Affichage des scores en temps réel
- [x] Gestion de la fin de partie avec gagnant
- [x] Boutons d'action (Vérifier, Annuler, Nouvelle Partie)

## 🎮 Modes de Jeu

### Mode Console (TrioConsole.java)
Jeu en ligne de commande avec:
- Affichage textuel des cartes
- Sélection par indice (0-11)
- Validation par ligne de commande
- Parfait pour tester la logique

### Mode Graphique (TrioGUI.java)
Interface Swing avec:
- Grille de cartes interactive
- Sélection par clic souris
- Cartes mis en évidence lors de la sélection
- Scores affichés en temps réel
- Boutons intuitifs

## 📝 Classes Principales

### `model/Carte.java`
Représente une unique carte:
- 4 attributs: valeur, forme, couleur, remplissage
- Getters pour accéder aux propriétés
- Affichage formaté

### `model/Joueur.java`
Gère les informations du joueur:
- Nom et ID unique
- Score et statut
- Méthodes pour ajuster le score

### `model/Deck.java`
Gère le paquet de 81 cartes:
- Génération automatique de toutes les cartes
- Mélange aléatoire
- Tirage progressif des cartes
- Réinitialisation

### `game/Trio.java`
Logique principale du jeu:
- Gestion des états (Attente, En cours, Terminée)
- Validation des trios avec algorithme d'attributs
- Gestion des sélections et des tours
- Contrôle de la fin de partie

### `ui/TrioConsole.java`
Interface texte:
- Boucle de jeu interactive
- Affichage console formaté
- Gestion des entrées utilisateur

### `ui/TrioGUI.java`
Interface graphique Swing:
- JFrame principale
- Panneaux pour cartes, scores, actions
- Inner class CartePanel pour chaque carte
- Gestion des événements souris

## 🧪 Tests et Validation

Pour tester le jeu:

1. **Mode Console**: 
   ```bash
   java -cp build/classes ui.TrioConsole
   ```
   Suivez les instructions pour sélectionner et valider des trios

2. **Mode Graphique**:
   ```bash
   java -jar build/jar/Trio.jar
   ```
   Cliquez sur les cartes pour les sélectionner, puis vérifiez

## 🎨 Interface Utilisateur

### Mode Console
```
═══════════════════════════════════════════
        BIENVENUE AU JEU TRIO - UTBM
═══════════════════════════════════════════

[0] 🔴● ... (affichage texte des cartes)
[1] 🟢■
...
```

### Mode Graphique
- Cartes affichées dans une grille colorée
- Cartes sélectionnées surlignées en jaune
- Scores et joueur actuel en haut
- Boutons d'action en bas

## 📋 Exemple d'Utilisation

### Trio Valide
```
Sélection:
  - Carte 0: 1 ● Rouge Plein
  - Carte 5: 2 ● Vert Plein
  - Carte 10: 3 ● Violet Plein

Résultat: ✓ VALIDE
(Même forme, Valeur différente, Couleur différente, Remplissage identique)
```

### Trio Invalide
```
Sélection:
  - Carte 1: 1 ● Rouge Plein
  - Carte 2: 1 ■ Rouge Plein
  - Carte 3: 1 〰 Vert Plein

Résultat: ✗ INVALIDE
(Couleur: 2 rouges et 1 vert → ni identique ni tous différents)
```

## 🔍 Algorithme de Validation de Trio

```java
for each attribute (valeur, forme, couleur, remplissage):
  if (v1 == v2 == v3):        // Tous identiques
    continue
  else if (v1 ≠ v2 ≠ v3):    // Tous différents
    continue
  else:                        // Mélange
    return FALSE              // Invalide
return TRUE                    // Trio valide
```

## 📚 Documentation Technique

- Tous les fichiers Java incluent des **JavaDoc complets**
- Structure conforme aux **conventions Java**
- Utilisation de **CamelCase** pour les noms
- Commentaires explicatifs dans le code complexe

## 🎯 Améliorations Futures

- Sauvegarde/chargement de parties
- Multijoueur en réseau (Sockets)
- Thème graphique complet UTBM
- Animations et effets visuels
- Base de données pour statistiques
- Mode solo avec IA
- Système de achievements

## ✅ Checklist de Remise (09 Janvier 2026)

- [x] Code source complet en Java
- [x] Classes bien structurées et documentées
- [x] Diagrammes UML (fichiers séparés)
- [x] Interface console fonctionnelle (Séance 2)
- [x] Interface GUI fonctionnelle (Séance 3)
- [x] Fichier JAR exécutable
- [x] Scripts de compilation (Linux + Windows)
- [x] Documentation README complète
- [ ] Rapport de conception UML (à finaliser)
- [ ] Vidéo de démonstration (~5 min)

## 📞 Support

Pour toute question:
1. Consultez les commentaires JavaDoc dans chaque classe
2. Vérifiez les scripts build.sh et build.bat
3. Testez en mode console pour isoler les problèmes

---

**Créé pour le projet AP4B - Automne 2025**  
**Université de Technologie de Belfort-Montbéliard (UTBM)**  
**Langage: Java**  
**Architecture: MVC avec Swing GUI**
