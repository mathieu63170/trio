# Architecture Client/Serveur - Trio

## Structure du projet

```
trio/
├── src/                          # Code source partagé
│   ├── model/                    # Classes de modèle (Carte, Joueur, Deck, Enums)
│   ├── game/                     # Logique du jeu (Trio.java)
│   └── ui/                       # Interfaces utilisateur
│       ├── TrioConsole.java      # Mode console (mono-joueur/test)
│       └── TrioGUI.java          # Mode GUI (mono-joueur/test)
│
├── client/                       # Code client
│   ├── TrioClient.java           # Client réseau
│   └── TrioClientGUI.java        # Interface graphique client
│
├── serveur/                      # Code serveur
│   ├── TrioServer.java           # Serveur principal
│   ├── ClientHandler.java        # Gestionnaire de connexion client
│   └── TrioPartieServeur.java    # Logique de partie côté serveur
│
└── commun/                       # Dossier partagé (configurations, etc.)
```

## Architecture Client-Serveur

### Communication

```
Client 1                    Serveur                    Client 2
   |                           |                          |
   |------- JOIN:Joueur1 ------>|                         |
   |                           |                          |
   |                           |<------ JOIN:Joueur2 -----|
   |                           |                          |
   |<-- CONNECTED:Bienvenue ---|                          |
   |                           |------ CONNECTED:Bien ----|
   |                           |                          |
   |--- SELECT:3 ------------>|                           |
   |                           |--- GAME_STATE -------->| |
   |                           |                          |
   |<----- GAME_STATE ---------|                          |
   |                           |<---- SELECT:7 ----------|
```

### Protocole de Communication

**Format**: `COMMAND:DATA`

#### Client → Serveur

| Commande | Paramètre | Description |
|----------|-----------|-------------|
| JOIN | nom_joueur | Connexion au serveur |
| SELECT | index | Sélectionner une carte (0-11) |
| VERIFY | - | Vérifier le trio |
| CANCEL | - | Annuler la sélection |
| NEW_GAME | - | Demander une nouvelle partie |

#### Serveur → Client

| Commande | Paramètre | Description |
|----------|-----------|-------------|
| CONNECTED | message | Confirmation de connexion |
| GAME_STATE | état_json | État actuel du jeu |
| CARDS | cartes_json | Liste des cartes en jeu |
| SCORES | scores_json | Scores des joueurs |
| MESSAGE | texte | Message au client |
| GAME_OVER | gagnant | Fin de partie |
| ERROR | description | Erreur |

## Compilation

### Compiler tout

```bash
# Linux/Mac
bash build.sh

# Windows
build.bat
```

### Compiler sélectivement

```bash
# Compiler les sources
javac -d build/classes src/**/*.java client/*.java serveur/*.java

# Créer le JAR serveur
jar cfe build/jar/TrioServer.jar serveur.TrioServer -C build/classes .

# Créer le JAR client
jar cfe build/jar/TrioClient.jar client.TrioClientGUI -C build/classes .
```

## Exécution

### Mode Serveur

```bash
# Démarrer le serveur
java -cp build/classes serveur.TrioServer
```

Le serveur s'exécute sur le port **5000** par défaut.

### Mode Client

Dans une autre fenêtre:

```bash
# Démarrer un client
java -cp build/classes client.TrioClientGUI

# Ou avec paramètres personnalisés
java -cp build/classes client.TrioClientGUI <host> <port>
```

Exemple avec plusieurs clients:

```bash
# Terminal 1: Serveur
java -cp build/classes serveur.TrioServer

# Terminal 2: Client 1
java -cp build/classes client.TrioClientGUI localhost 5000

# Terminal 3: Client 2
java -cp build/classes client.TrioClientGUI localhost 5000
```

## Modes de Jeu

### 1. Mode Console (src/ui/TrioConsole.java)
- Interface texte
- Joueur(s) local(aux)
- Parfait pour tester la logique
- Commande: `java -cp build/classes ui.TrioConsole`

### 2. Mode GUI Local (src/ui/TrioGUI.java)
- Interface Swing
- Joueur(s) local(aux)
- Affichage visuel des cartes
- Commande: `java -jar build/jar/Trio.jar`

### 3. Mode Multijoueur Réseau (Client/Serveur)
- Clients qui se connectent au serveur
- Synchronisation en temps réel
- Plusieurs joueurs simultanément
- Commandes: voir "Exécution" ci-dessus

## Classes Principales

### Côté Client

**TrioClient**
- Gère la connexion socket
- Envoie les actions du joueur
- Reçoit les mises à jour du serveur
- Communique avec la GUI

**TrioClientGUI**
- Interface Swing pour le client
- Panel de connexion
- Panel de jeu (cartes, boutons)
- Affichage des messages

### Côté Serveur

**TrioServer**
- Accepte les connexions
- Gère la liste des clients
- Crée et démarre les parties
- Diffuse l'état du jeu

**ClientHandler**
- Gère chaque connexion client
- Traite les commandes reçues
- Envoie les réponses

**TrioPartieServeur**
- Logique de partie côté serveur
- Gestion des sélections
- Validation des trios
- État du jeu

## Flux de Jeu Multijoueur

```
1. Joueur 1 se connecte → CONNECTED reçu
2. Joueur 2 se connecte → Partie démarre (2 joueurs min)
3. Serveur: GAME_STATE envoyé à tous
4. Joueur 1: SELECT 3 → Serveur traite
5. Serveur: GAME_STATE envoyé à Joueur 1 et 2
6. Joueur 2: SELECT 7 → Serveur traite
7. Joueur 1: VERIFY → Serveur valide et distribue points
8. Serveur: GAME_STATE + MESSAGE envoyé à tous
9. Jusqu'à fin de partie...
10. Serveur: GAME_OVER + gagnant
```

## Futures Améliorations

- [ ] Authentification des joueurs
- [ ] Persistance des scores (base de données)
- [ ] Système de lobby (attendre plusieurs joueurs)
- [ ] Chat intégré
- [ ] Replay des parties
- [ ] Classement global
- [ ] Support du jeu avec IA

---

**Documentation Architecture Client-Serveur**  
Novembre 2025
