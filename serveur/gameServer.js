// Données et état du jeu côté serveur

const Trio = require('../commun/trio.js');

class GameServer {
  constructor() {
    this.jeux = {}; // Stocke les jeux par roomId
    this.joueurs = {}; // Stocke les joueurs par socketId
  }

  creerJeu(roomId) {
    const trio = new Trio();
    trio.melangerCartes();
    
    this.jeux[roomId] = {
      id: roomId,
      cartes: trio.obtenirCartesInitiales(),
      cartesRestantes: trio.cartes.slice(12),
      cartesEnJeu: [],
      trio: trio,
      joueurs: [],
      scores: {},
      carteSelectionnees: [],
      etat: 'attente' // attente, en_cours, termine
    };

    return this.jeux[roomId];
  }

  ajouterJoueur(roomId, socketId, nom) {
    if (!this.jeux[roomId]) {
      this.creerJeu(roomId);
    }

    this.joueurs[socketId] = {
      id: socketId,
      nom: nom,
      roomId: roomId,
      score: 0,
      connecte: true
    };

    if (!this.jeux[roomId].scores[socketId]) {
      this.jeux[roomId].scores[socketId] = 0;
    }

    this.jeux[roomId].joueurs.push(socketId);
    
    return this.jeux[roomId];
  }

  retirerJoueur(socketId) {
    if (this.joueurs[socketId]) {
      const roomId = this.joueurs[socketId].roomId;
      if (this.jeux[roomId]) {
        this.jeux[roomId].joueurs = this.jeux[roomId].joueurs.filter(id => id !== socketId);
        if (this.jeux[roomId].joueurs.length === 0) {
          delete this.jeux[roomId];
        }
      }
      delete this.joueurs[socketId];
    }
  }

  selectionnerCarte(roomId, socketId, indexCarte) {
    const jeu = this.jeux[roomId];
    if (!jeu) return null;

    // Éviter les doublons
    if (jeu.carteSelectionnees.includes(indexCarte)) {
      jeu.carteSelectionnees = jeu.carteSelectionnees.filter(idx => idx !== indexCarte);
    } else {
      jeu.carteSelectionnees.push(indexCarte);
    }

    return jeu.carteSelectionnees;
  }

  verifierTrio(roomId, socketId) {
    const jeu = this.jeux[roomId];
    if (!jeu || jeu.carteSelectionnees.length !== 3) return null;

    const indices = jeu.carteSelectionnees;
    const carte1 = jeu.cartes[indices[0]];
    const carte2 = jeu.cartes[indices[1]];
    const carte3 = jeu.cartes[indices[2]];

    const valide = jeu.trio.estTrioValide(carte1, carte2, carte3);

    if (valide) {
      // Augmente le score du joueur
      jeu.scores[socketId]++;

      // Supprime les cartes (du plus grand indice au plus petit pour éviter les problèmes)
      indices.sort((a, b) => b - a);
      indices.forEach(idx => {
        jeu.cartes.splice(idx, 1);
      });

      // Ajoute 3 nouvelles cartes si disponibles
      const nouvelles = jeu.cartesRestantes.splice(0, 3);
      jeu.cartes.push(...nouvelles);

      // Réinitialise la sélection
      jeu.carteSelectionnees = [];

      // Vérifie s'il reste des cartes
      if (jeu.cartes.length === 0 && jeu.cartesRestantes.length === 0) {
        jeu.etat = 'termine';
      }
    } else {
      jeu.carteSelectionnees = [];
    }

    return { valide, scores: jeu.scores };
  }

  obtenirEtatJeu(roomId) {
    return this.jeux[roomId] || null;
  }
}

module.exports = GameServer;
