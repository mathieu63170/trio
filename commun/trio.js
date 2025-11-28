// Logiques communes du jeu Trio

class Trio {
  constructor() {
    // Les 81 cartes : 3 valeurs (1,2,3) x 3 formes x 3 couleurs x 3 remplissages
    this.cartes = [];
    this.initCartes();
  }

  initCartes() {
    // Valeurs possibles
    const valeurs = [1, 2, 3];
    const formes = ['cercle', 'carre', 'ondulation'];
    const couleurs = ['rouge', 'vert', 'violet'];
    const remplissages = ['plein', 'vide', 'rayes'];

    let id = 0;
    for (let val of valeurs) {
      for (let forme of formes) {
        for (let couleur of couleurs) {
          for (let remplissage of remplissages) {
            this.cartes.push({
              id: id++,
              valeur: val,
              forme: forme,
              couleur: couleur,
              remplissage: remplissage
            });
          }
        }
      }
    }
  }

  // Vérifie si trois cartes forment un trio valide
  estTrioValide(carte1, carte2, carte3) {
    const cartes = [carte1, carte2, carte3];

    // Vérifie chaque attribut
    const attributs = ['valeur', 'forme', 'couleur', 'remplissage'];

    for (let attr of attributs) {
      const val1 = cartes[0][attr];
      const val2 = cartes[1][attr];
      const val3 = cartes[2][attr];

      // Soit les 3 sont identiques, soit tous différents
      const sontIdentiques = val1 === val2 && val2 === val3;
      const sontDifferents = val1 !== val2 && val2 !== val3 && val1 !== val3;

      if (!sontIdentiques && !sontDifferents) {
        return false;
      }
    }

    return true;
  }

  // Mélange les cartes
  melangerCartes() {
    for (let i = this.cartes.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [this.cartes[i], this.cartes[j]] = [this.cartes[j], this.cartes[i]];
    }
    return this.cartes;
  }

  // Récupère les 12 premières cartes
  obtenirCartesInitiales() {
    return this.cartes.slice(0, 12);
  }
}

if (typeof module !== 'undefined' && module.exports) {
  module.exports = Trio;
}
