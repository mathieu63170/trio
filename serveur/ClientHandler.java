package serveur;

import commun.action.*;
import commun.plateau.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Classe ClientHandler : gère la communication avec un client
 * Utilise la sérialisation Java pour envoyer les objets
 */
public class ClientHandler implements Runnable {
    private final Serveur serveur;
    private final Socket socket;
    private final int idJoueur;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Joueur joueur;
    private boolean actif;

    public ClientHandler(Serveur serveur, Socket socket, int idJoueur) {
        this.serveur = serveur;
        this.socket = socket;
        this.idJoueur = idJoueur;
        this.actif = true;
    }

    @Override
    public void run() {
        try {
            // Initialiser les streams (Output avant Input!)
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Envoyer l'ID du joueur sous forme de String
            out.writeObject("ID:" + idJoueur);
            out.flush();
            
            // Créer le joueur
            joueur = new Joueur(idJoueur, "Joueur" + idJoueur, new ArrayList<>(), new ArrayList<>());
            
            System.out.println("✓ Client " + idJoueur + " connecté et initialisé");
            
            // Boucle de réception
            while (actif && !socket.isClosed()) {
                Object obj = in.readObject();
                traiterMessage(obj);
            }
            
        } catch (EOFException e) {
            System.out.println("✓ Client " + idJoueur + " fermé proprement");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("✗ Erreur client " + idJoueur + ": " + e.getMessage());
        } finally {
            fermer();
        }
    }

    /**
     * Traite un message reçu du client
     */
    private void traiterMessage(Object obj) {
        if (obj instanceof Action) {
            Action action = (Action) obj;
            System.out.println("📨 Action reçue du joueur " + idJoueur + ": " + action.getClass().getSimpleName());
            serveur.traiterAction(this, action);
            
        } else if (obj instanceof String) {
            String cmd = (String) obj;
            System.out.println("📨 Commande reçue du joueur " + idJoueur + ": " + cmd);
            traiterCommande(cmd);
        }
    }

    /**
     * Traite une commande texte
     */
    private void traiterCommande(String cmd) {
        if (cmd.equals("GET_PLATEAU")) {
            envoyerPlateau();
        } else if (cmd.equals("GET_PLAYER_INFO")) {
            envoyerInfoJoueur();
        } else if (cmd.equals("PING")) {
            envoyerMessage("PONG");
        } else if (cmd.equals("GET_OTHER_PLAYERS_CARDS")) {
            envoyerCartesAutresJoueurs();
        }
    }

    /**
     * Envoie le plateau au client
     */
    public void envoyerPlateau(Plateau plateau) {
        try {
            System.out.println("📤 ENVOI PLATEAU - Phase: " + plateau.getPhaseActuelle() + ", Gagnant: " + plateau.getGagnant());
            out.writeObject(plateau);
            out.reset();  // Important pour éviter les problèmes de cache
            out.flush();
            System.out.println("✅ PLATEAU ENVOYÉ");
        } catch (IOException e) {
            System.err.println("✗ Erreur d'envoi plateau: " + e.getMessage());
        }
    }

    /**
     * Envoie le plateau actuel du serveur
     */
    public void envoyerPlateau() {
        envoyerPlateau(serveur.getPlateau());
    }

    /**
     * Envoie les infos du joueur
     */
    public void envoyerInfoJoueur() {
        try {
            out.writeObject(joueur);
            out.reset();
            out.flush();
        } catch (IOException e) {
            System.err.println("✗ Erreur d'envoi info joueur: " + e.getMessage());
        }
    }

    /**
     * Envoie un message texte au client
     */
    public void envoyerMessage(String message) {
        try {
            out.writeObject(message);
            out.reset();
            out.flush();
        } catch (IOException e) {
            System.err.println("✗ Erreur d'envoi message: " + e.getMessage());
        }
    }

    /**
     * Envoie un objet sérialisable au client
     */
    public void envoyer(Object obj) {
        try {
            out.writeObject(obj);
            out.reset();
            out.flush();
        } catch (IOException e) {
            System.err.println("✗ Erreur d'envoi: " + e.getMessage());
        }
    }

    /**
     * Envoie les cartes des autres joueurs
     */
    public void envoyerCartesAutresJoueurs() {
        try {
            // Récupérer tous les joueurs du serveur
            java.util.List<Joueur> tousLesJoueurs = serveur.getTousLesJoueurs();
            
            // Créer une liste sans nous-mêmes
            java.util.List<Joueur> autresJoueurs = new java.util.ArrayList<>();
            for (Joueur j : tousLesJoueurs) {
                if (j.getId() != this.idJoueur) {
                    autresJoueurs.add(j);
                }
            }
            
            out.writeObject(autresJoueurs);
            out.reset();
            out.flush();
            
            System.out.println("✓ Cartes de " + autresJoueurs.size() + " autres joueur(s) envoyées au joueur " + idJoueur);
        } catch (IOException e) {
            System.err.println("✗ Erreur d'envoi cartes autres joueurs: " + e.getMessage());
        }
    }

    /**
     * Ferme la connexion
     */
    public void fermer() {
        actif = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("✗ Erreur fermeture socket: " + e.getMessage());
        }
    }

    // Getters
    public int getIdJoueur() {
        return idJoueur;
    }

    public Joueur getJoueur() {
        return joueur;
    }

    public void setJoueur(Joueur joueur) {
        this.joueur = joueur;
    }

    public boolean isActif() {
        return actif && !socket.isClosed();
    }
}

