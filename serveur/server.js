const express = require('express');
const http = require('http');
const socketIO = require('socket.io');
const path = require('path');
const GameServer = require('./gameServer.js');

const app = express();
const server = http.createServer(app);
const io = socketIO(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
});

const gameServer = new GameServer();

// Middleware
app.use(express.static(path.join(__dirname, '../client')));

// Routes
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, '../client/index.html'));
});

app.get('/api/sante', (req, res) => {
  res.json({ status: 'ok', message: 'Serveur Trio actif' });
});

// WebSocket - Événements
io.on('connection', (socket) => {
  console.log(`Joueur connecté: ${socket.id}`);

  socket.on('rejoindre_jeu', (data) => {
    const { roomId, nom } = data;
    socket.join(roomId);

    const jeu = gameServer.ajouterJoueur(roomId, socket.id, nom);

    console.log(`${nom} a rejoint la room ${roomId}`);

    // Notifie les autres joueurs
    io.to(roomId).emit('joueur_rejoint', {
      joueur: {
        id: socket.id,
        nom: nom
      },
      joueurs: jeu.joueurs.map(id => ({
        id: id,
        nom: gameServer.joueurs[id].nom,
        score: jeu.scores[id]
      })),
      cartes: jeu.cartes
    });
  });

  socket.on('selectionner_carte', (data) => {
    const { roomId, indexCarte } = data;
    const jeu = gameServer.obtenirEtatJeu(roomId);

    if (!jeu) return;

    gameServer.selectionnerCarte(roomId, socket.id, indexCarte);

    io.to(roomId).emit('mise_a_jour_selection', {
      cartesSelectionnees: jeu.carteSelectionnees
    });
  });

  socket.on('verifier_trio', (data) => {
    const { roomId } = data;
    const resultat = gameServer.verifierTrio(roomId, socket.id);

    if (resultat) {
      const jeu = gameServer.obtenirEtatJeu(roomId);

      io.to(roomId).emit('resultat_trio', {
        valide: resultat.valide,
        scores: resultat.scores,
        cartes: jeu.cartes,
        cartesRestantes: jeu.cartesRestantes.length,
        cartesSelectionnees: jeu.carteSelectionnees,
        etat: jeu.etat,
        gagnant: resultat.valide ? gameServer.joueurs[socket.id].nom : null
      });
    }
  });

  socket.on('deconnexion_prevue', () => {
    // À implémenter
  });

  socket.on('disconnect', () => {
    console.log(`Joueur déconnecté: ${socket.id}`);
    gameServer.retirerJoueur(socket.id);
    
    // Notifie les autres joueurs
    for (const roomId in io.sockets.adapter.rooms) {
      if (io.sockets.adapter.rooms[roomId].has(socket.id)) {
        io.to(roomId).emit('joueur_parti', {
          id: socket.id
        });
      }
    }
  });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`Serveur Trio lancé sur le port ${PORT}`);
  console.log(`Accédez à l'application: http://localhost:${PORT}`);
});
