# Documentation UML - Projet Trio AP4B

## Diagramme de Classes Complet

```
╔════════════════════════════════════════════════════════════════════════════╗
║                         PACKAGE: model                                    ║
╚════════════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────┐
│          <<enum>> Forme             │
├─────────────────────────────────────┤
│ CERCLE                              │
│ CARRE                               │
│ ONDULATION                          │
├─────────────────────────────────────┤
│ + getSymbole(): String              │
│ + toString(): String                │
└─────────────────────────────────────┘
         ▲
         │ utilise
         │

┌─────────────────────────────────────┐
│         <<enum>> Couleur            │
├─────────────────────────────────────┤
│ ROUGE                               │
│ VERT                                │
│ VIOLET                              │
├─────────────────────────────────────┤
│ + getEmoji(): String                │
│ + getNom(): String                  │
│ + toString(): String                │
└─────────────────────────────────────┘
         ▲
         │ utilise
         │

┌────────────────────────────────────────┐
│      <<enum>> Remplissage             │
├────────────────────────────────────────┤
│ PLEIN                                  │
│ VIDE                                   │
│ RAYES                                  │
├────────────────────────────────────────┤
│ + getNom(): String                     │
│ + toString(): String                   │
└────────────────────────────────────────┘
         ▲
         │ utilise
         │

┌──────────────────────────────────────────┐
│              Carte                       │
├──────────────────────────────────────────┤
│ - id: int                                │
│ - valeur: int {1..3}                    │
│ - forme: Forme                           │
│ - couleur: Couleur                       │
│ - remplissage: Remplissage               │
├──────────────────────────────────────────┤
│ + getId(): int                           │
│ + getValeur(): int                       │
│ + getForme(): Forme                      │
│ + getCouleur(): Couleur                  │
│ + getRemplissage(): Remplissage          │
│ + toString(): String                     │
│ + toShortString(): String                │
└──────────────────────────────────────────┘


┌───────────────────────────────────────┐
│         Joueur                        │
├───────────────────────────────────────┤
│ - id: String                          │
│ - nom: String                         │
│ - score: int = 0                      │
│ - actif: boolean = true               │
├───────────────────────────────────────┤
│ + getId(): String                     │
│ + getNom(): String                    │
│ + getScore(): int                     │
│ + isActif(): boolean                  │
│ + setActif(boolean): void             │
│ + ajouterPoint(): void                │
│ + ajouterPoints(int): void            │
│ + reinitialiserScore(): void          │
│ + equals(Object): boolean             │
│ + hashCode(): int                     │
└───────────────────────────────────────┘


┌────────────────────────────────────────────┐
│         Deck                               │
├────────────────────────────────────────────┤
│ - cartes: List<Carte>                      │
│ - TOTAL_CARTES: int = 81                   │
├────────────────────────────────────────────┤
│ + Deck()                                   │
│ - genererCartes(): void                    │
│ + melangerCartes(): void                   │
│ + tirerCarte(): Carte                      │
│ + tirerCartes(int): List<Carte>            │
│ + getNombreCartesRestantes(): int          │
│ + estVide(): boolean                       │
│ + getCartes(): List<Carte>                 │
│ + reinitialiser(): void                    │
└────────────────────────────────────────────┘
         │
         │ contient 81
         │ ▼
      Carte


╔════════════════════════════════════════════════════════════════════════════╗
║                         PACKAGE: game                                     ║
╚════════════════════════════════════════════════════════════════════════════╝

┌──────────────────────────────────────────────────────┐
│   <<enum>> EtatJeu                                  │
├──────────────────────────────────────────────────────┤
│ ATTENTE                                              │
│ EN_COURS                                             │
│ TERMINE                                              │
└──────────────────────────────────────────────────────┘
         ▲
         │
         │

┌──────────────────────────────────────────────────────────────────────┐
│                        Trio                                          │
├──────────────────────────────────────────────────────────────────────┤
│ - deck: Deck                                                         │
│ - cartesEnJeu: List<Carte>                                          │
│ - joueurs: List<Joueur>                                             │
│ - joueurActuel: Joueur                                              │
│ - cartesSelectionnees: List<Integer>                                │
│ - etat: EtatJeu                                                     │
│ - CARTES_INITIALES: int = 12                                        │
├──────────────────────────────────────────────────────────────────────┤
│ + Trio(List<Joueur>)                                                │
│ + demarrerPartie(): void                                            │
│ + estTrioValide(Carte, Carte, Carte): boolean                       │
│ - verifierAttribut<T>(T, T, T): boolean                             │
│ + selectionnerCarte(int): void                                      │
│ + verifierTrio(): boolean                                           │
│ + joueurSuivant(): void                                             │
│ + getCartesEnJeu(): List<Carte>                                     │
│ + getJoueurs(): List<Joueur>                                        │
│ + getJoueurActuel(): Joueur                                         │
│ + getCartesSelectionnees(): List<Integer>                           │
│ + getNombreCartesRestantes(): int                                   │
│ + getEtat(): EtatJeu                                                │
│ + estTerminee(): boolean                                            │
└──────────────────────────────────────────────────────────────────────┘
         │
         ├─── utilise ──────┬──────────┬──────────┐
         ▼                  ▼          ▼          ▼
       Deck              Carte      Joueur    EtatJeu


╔════════════════════════════════════════════════════════════════════════════╗
║                         PACKAGE: ui                                       ║
╚════════════════════════════════════════════════════════════════════════════╝

┌──────────────────────────────────────────────────────────┐
│         TrioConsole                                      │
├──────────────────────────────────────────────────────────┤
│ - jeu: Trio                                              │
│ - scanner: Scanner                                       │
│ - joueurs: List<Joueur>                                 │
├──────────────────────────────────────────────────────────┤
│ + TrioConsole()                                          │
│ + lancer(): void                                         │
│ - afficherBienvenue(): void                              │
│ - initialiserJoueurs(): void                             │
│ - jouerPartie(): void                                    │
│ - afficherEtatJeu(): void                                │
│ - afficherCartes(): void                                 │
│ - afficherScores(): void                                 │
│ - effectuerAction(): void                                │
│ - verifierTrio(): void                                   │
│ - afficherResultats(): void                              │
│ - lireEntier(int, int): int                              │
│ + main(String[]): void                                   │
└──────────────────────────────────────────────────────────┘
         │
         └─── utilise ───┬──────────┐
                         ▼          ▼
                       Trio      Joueur


┌────────────────────────────────────────────────────────────────┐
│            TrioGUI (extends JFrame)                           │
├────────────────────────────────────────────────────────────────┤
│ - jeu: Trio                                                    │
│ - joueurs: List<Joueur>                                       │
│ - panneauxCartes: CartePanel[]                                │
│ - labelJoueurActuel: JLabel                                   │
│ - labelScores: JLabel                                         │
│ - btnVerifier: JButton                                        │
│ - btnAnnuler: JButton                                         │
│ - btnNouvelle: JButton                                        │
│ - labelMessage: JLabel                                        │
│ - CARTES_PAR_LIGNE: int = 4                                   │
├────────────────────────────────────────────────────────────────┤
│ + TrioGUI()                                                    │
│ - initialiserJoueurs(): void                                  │
│ - construireInterface(): void                                 │
│ - creerPanneauHaut(): JPanel                                  │
│ - creerPanneauCartes(): JPanel                                │
│ - creerPanneauBas(): JPanel                                   │
│ - afficherCartes(): void                                      │
│ - afficherScores(): void                                      │
│ - verifierTrio(): void                                        │
│ - annulerSelection(): void                                    │
│ - nouvellePartie(): void                                      │
│ - terminerPartie(): void                                      │
│ - afficherMessage(String, Color): void                        │
│ + main(String[]): void                                        │
├────────────────────────────────────────────────────────────────┤
│                     Inner Class                                │
│              ┌─────────────────────────────┐                  │
│              │ CartePanel                  │                  │
│              ├─────────────────────────────┤                  │
│              │ - index: int                │                  │
│              │ - carte: Carte              │                  │
│              │ - selectionnee: boolean     │                  │
│              ├─────────────────────────────┤                  │
│              │ + setCarte(Carte): void     │                  │
│              │ + setSelectionnee(b): void  │                  │
│              │ + paintComponent(Graphics)  │                  │
│              └─────────────────────────────┘                  │
└────────────────────────────────────────────────────────────────┘
         │
         └─── utilise ──────┬──────────┐
                            ▼          ▼
                          Trio      Joueur
```

## Diagramme de Cas d'Utilisation

```
┌────────────────────────────────────────────────────────────────┐
│                    Système Trio                                │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│   ┌────────────────────────────────────────────────────────┐  │
│   │              Lancer Partie                             │  │
│   └────────────┬───────────────────────────────────────────┘  │
│                │                                               │
│   ┌────────────▼──────────┐        ┌──────────────────────┐  │
│   │  Sélectionner Cartes  │        │   Vérifier Trio      │  │
│   └──────────────────────┘        └──────────────────────┘  │
│                │                          │                  │
│                │                          ▼                  │
│                │                   ┌──────────────────────┐  │
│                │                   │  Calculer Score      │  │
│                │                   └──────────────────────┘  │
│                │                                               │
│                ▼                                               │
│       ┌──────────────────────┐                                │
│       │  Afficher Résultats  │                                │
│       └──────────────────────┘                                │
│                                                                │
└────────────────────────────────────────────────────────────────┘

        ▲ acteur                ▲ acteur
        │                        │
        │                        │
     Joueur                  Joueur
```

## Diagramme de Séquence: Vérification d'un Trio

```
Joueur      TrioGUI           Trio          Deck
  │           │                │             │
  │ clic      │                │             │
  ├──────────>│                │             │
  │ clic      │ selectionnerCarte(0)        │
  ├──────────>│───────────────>│             │
  │ clic      │                │             │
  ├──────────>│ selectionnerCarte(5)        │
  │           │───────────────>│             │
  │           │                │             │
  │ clic      │ selectionnerCarte(10)       │
  │ Vérifier  │───────────────>│             │
  ├──────────>│                │             │
  │           │ verifierTrio() │             │
  │           │───────────────>│             │
  │           │                │             │
  │           │<───────────────│ true        │
  │           │                │             │
  │           │ ajouterPoint() │             │
  │           │───────────────>│             │
  │           │                │             │
  │           │                │ tirerCartes(3)
  │           │                │────────────>│
  │           │                │<────────────│ [Carte, Carte, Carte]
  │           │                │             │
  │<──────────│ rafraichir()    │             │
  │ afficher  │ Trio trouvé! +1 point       │
```

## Associations et Relations

### 1. Composition
- **Trio** contient **Deck** (1:1)
- **Trio** contient **List<Joueur>** (1:N)
- **Trio** contient **List<Carte>** (1:N)

### 2. Utilisation
- **Carte** utilise **Forme**, **Couleur**, **Remplissage** (énums)
- **Deck** génère **Carte** (81 instances)
- **Trio** utilise **Carte**, **Joueur**, **Deck**

### 3. Héritage
- Aucun héritage dans le modèle (architecture plate et simple)

## Patterns de Conception Utilisés

### 1. Factory Pattern
```
Deck.genererCartes()
  ├─ crée 81 instances de Carte
  └─ chaque Carte = combinaison unique des 4 attributs
```

### 2. MVC Pattern
```
Model:     Carte, Joueur, Deck, Trio
View:      TrioConsole, TrioGUI (interface)
Control:   Trio (logique métier)
```

### 3. Enumeration Pattern
```
Forme, Couleur, Remplissage = énums pour type-safety
```

### 4. State Pattern
```
EtatJeu { ATTENTE, EN_COURS, TERMINE }
  └─ Transition: ATTENTE → EN_COURS → TERMINE
```

## Algorithme de Validation de Trio

```java
boolean estTrioValide(Carte c1, Carte c2, Carte c3) {
  
  for each attribut in [valeur, forme, couleur, remplissage] {
    
    boolean sontIdentiques = 
      c1.attribut == c2.attribut AND c2.attribut == c3.attribut
    
    boolean sontDifferents = 
      c1.attribut != c2.attribut AND 
      c2.attribut != c3.attribut AND 
      c1.attribut != c3.attribut
    
    if NOT (sontIdentiques OR sontDifferents) {
      return FALSE  // Invalide pour cet attribut
    }
  }
  
  return TRUE  // Trio valide pour tous les attributs
}
```

## Flux de Jeu

```
┌─────────────────────────────────────────────────────────┐
│ Démarrer Partie                                         │
├─────────────────────────────────────────────────────────┤
│ 1. Créer Deck (81 cartes)                               │
│ 2. Mélanger cartes                                      │
│ 3. Tirer 12 cartes initiales                            │
│ 4. Initialiser joueurs (score = 0)                      │
│ 5. Afficher interface                                   │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
         ┌───────────────────────────────────────────────┐
         │ Boucle de Jeu (Tant que jeu non terminé)     │
         ├───────────────────────────────────────────────┤
         │ 1. Afficher joueur actuel                    │
         │ 2. Afficher 12 cartes en jeu                 │
         │ 3. Attendre sélection (0-3 cartes)           │
         │ 4. Joueur sélectionne 3 cartes               │
         │ 5. Vérifier si trio valide                   │
         │    OUI: +1 point, remplacer 3 cartes         │
         │    NON: réinitialiser sélection              │
         │ 6. Passer au joueur suivant                  │
         └────────┬─────────────────────────────────────┘
                  │
      ┌───────────┴────────────────┐
      │                            │
   Cartes │                        │
   vides  │                   Deck │ Cartes
      &   │                    restantes │
     deck │                        │
    vide  │ OUI                    │ NON
      │   │                        │
      ▼   ▼                        ▼
   ┌──────────────────────────────┐
   │ Fin de Partie                │
   ├──────────────────────────────┤
   │ 1. Calculer gagnant          │
   │ 2. Afficher résultats        │
   │ 3. Option: Nouvelle partie   │
   └──────────────────────────────┘
```

---

**Documentation UML - Projet AP4B Trio**  
**Date**: Novembre 2025
