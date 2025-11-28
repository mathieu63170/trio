// Logiques du jeu côté client

let socket;
let roomId;
let socketId;
let nomJoueur;
let cartesActuelles = [];
let cartesSelectionnees = [];
let partieEnCours = false;

// Connexion WebSocket
function initialiserSocket() {
  socket = io();

  socket.on('connect', () => {
    socketId = socket.id;
    console.log('Connecté au serveur');
  });

  socket.on('joueur_rejoint', (data) => {
    cartesActuelles = data.cartes;
    afficherCartes();
    afficherScores(data.joueurs);
    partieEnCours = true;
    document.getElementById('ecran-accueil').classList.remove('active');
    document.getElementById('ecran-jeu').classList.add('active');
    document.getElementById('room-id').textContent = `Room: ${roomId}`;
  });

  socket.on('mise_a_jour_selection', (data) => {
    cartesSelectionnees = data.cartesSelectionnees;
    afficherCartes();
    document.getElementById('btn-verifier').disabled = cartesSelectionnees.length !== 3;
  });

  socket.on('resultat_trio', (data) => {
    const messageDiv = document.getElementById('message');
    
    if (data.valide) {
      messageDiv.classList.remove('error');
      messageDiv.classList.add('success');
      messageDiv.textContent = `✓ Bravo ${data.gagnant}! +1 point`;
      
      // Met à jour les cartes
      cartesActuelles = data.cartes;
      cartesSelectionnees = [];
      afficherCartes();
      afficherScores([]);
      
      // Met à jour les stats
      document.getElementById('nb-cartes').textContent = data.cartes.length;
      document.getElementById('cartes-restantes').textContent = data.cartesRestantes;
      
      // Vérifie fin de partie
      if (data.etat === 'termine') {
        afficherFinPartie(data.scores);
      }
    } else {
      messageDiv.classList.remove('success');
      messageDiv.classList.add('error');
      messageDiv.textContent = '✗ Pas un trio valide!';
    }
    
    // Réinitialise le message après 3 secondes
    setTimeout(() => {
      messageDiv.classList.remove('success', 'error');
    }, 3000);
  });

  socket.on('joueur_parti', (data) => {
    console.log(`Le joueur ${data.id} est parti`);
  });
}

// Rejoindre un jeu
function rejoindreJeu() {
  nomJoueur = document.getElementById('nomJoueur').value.trim();
  const code = document.getElementById('codeRoom').value.trim();
  
  if (!nomJoueur) {
    alert('Veuillez entrer votre nom');
    return;
  }
  
  roomId = code || `room_${Date.now()}`;
  
  if (!socket) {
    initialiserSocket();
  }
  
  socket.emit('rejoindre_jeu', {
    roomId: roomId,
    nom: nomJoueur
  });
}

// Afficher les cartes
function afficherCartes() {
  const grille = document.getElementById('grille-cartes');
  grille.innerHTML = '';
  
  cartesActuelles.forEach((carte, index) => {
    const cartesDiv = creerCartesDiv(carte, index);
    grille.appendChild(cartesDiv);
  });
}

// Créer l'élément carte
function creerCartesDiv(carte, index) {
  const div = document.createElement('div');
  div.className = 'carte';
  if (cartesSelectionnees.includes(index)) {
    div.classList.add('selectionnee');
  }
  
  div.onclick = () => {
    socket.emit('selectionner_carte', {
      roomId: roomId,
      indexCarte: index
    });
  };
  
  // Couleur
  const couleurMap = {
    'rouge': '#e74c3c',
    'vert': '#27ae60',
    'violet': '#764ba2'
  };
  
  // Forme
  const formeSymbole = {
    'cercle': '●',
    'carre': '■',
    'ondulation': '〰'
  };
  
  // Remplissage
  const stylRemplissage = {
    'plein': { opacity: 1 },
    'vide': { opacity: 0.3 },
    'rayes': { opacity: 0.6 }
  };
  
  const symboleDiv = document.createElement('div');
  symboleDiv.className = 'symboles';
  
  const couleur = couleurMap[carte.couleur];
  const symbole = formeSymbole[carte.forme];
  
  for (let i = 0; i < carte.valeur; i++) {
    const span = document.createElement('span');
    span.className = 'symbole';
    span.textContent = symbole;
    span.style.color = couleur;
    span.style.opacity = stylRemplissage[carte.remplissage].opacity;
    span.style.textShadow = carte.remplissage === 'rayes' ? 
      `1px 1px 0 ${couleur}, 2px 2px 0 ${couleur}` : 'none';
    symboleDiv.appendChild(span);
  }
  
  const infoDiv = document.createElement('div');
  infoDiv.className = 'info-carte';
  infoDiv.textContent = `${carte.couleur} - ${carte.remplissage}`;
  
  div.appendChild(symboleDiv);
  div.appendChild(infoDiv);
  
  return div;
}

// Afficher les scores
function afficherScores(joueurs) {
  // À améliorer - récupérer depuis le serveur
  const scoresList = document.getElementById('scores-list');
  // À completer selon l'implémentation du serveur
}

// Vérifier le trio
function verifierTrio() {
  if (cartesSelectionnees.length === 3) {
    socket.emit('verifier_trio', {
      roomId: roomId
    });
  }
}

// Afficher fin de partie
function afficherFinPartie(scores) {
  const messageDiv = document.getElementById('message');
  messageDiv.classList.add('success');
  messageDiv.textContent = '🎉 Partie terminée!';
  
  document.getElementById('btn-verifier').style.display = 'none';
  document.getElementById('btn-nouvelle').style.display = 'block';
}

// Nouvelle partie
function nouvellePartie() {
  location.reload();
}

// Quitter le jeu
function quitterJeu() {
  if (confirm('Êtes-vous sûr de vouloir quitter?')) {
    socket.disconnect();
    document.getElementById('ecran-jeu').classList.remove('active');
    document.getElementById('ecran-accueil').classList.add('active');
    document.getElementById('nomJoueur').value = '';
    document.getElementById('codeRoom').value = '';
  }
}

// Initialisation au chargement
document.addEventListener('DOMContentLoaded', () => {
  // Interface prête
});
