package serveur;

import commun.action.*;
import commun.plateau.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


// Gère la connexion d'un client (thread séparé)
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
    // Écoute et traite les messages reçus du client
    public void run() {
        try {
            
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            
            out.writeObject("ID:" + idJoueur);
            out.flush();
            
            
            joueur = new Joueur(idJoueur, "Joueur" + idJoueur, new ArrayList<>(), new ArrayList<>());
            
            System.out.println(" Client " + idJoueur + " connecté et initialisé");
            
            
            while (actif && !socket.isClosed()) {
                Object obj = in.readObject();
                traiterMessage(obj);
            }
            
        } catch (EOFException e) {
            System.out.println(" Client " + idJoueur + " fermé proprement");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(" Erreur client " + idJoueur + ": " + e.getMessage());
        } finally {
            fermer();
        }
    }

    
    // Traite un objet reçu (Action ou commande texte)
    private void traiterMessage(Object obj) {
        if (obj instanceof Action) {
            Action action = (Action) obj;
            System.out.println(" Action reçue du joueur " + idJoueur + ": " + action.getClass().getSimpleName());
            serveur.traiterAction(this, action);
            
        } else if (obj instanceof String) {
            String cmd = (String) obj;
            System.out.println(" Commande reçue du joueur " + idJoueur + ": " + cmd);
            traiterCommande(cmd);
        }
    }

    
    // Exécute une commande simple reçue du client (GET_PLATEAU, PING, ...)
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

    
    // Envoie un objet `Plateau` précis au client
    public void envoyerPlateau(Plateau plateau) {
        try {
            System.out.println(" ENVOI PLATEAU - Phase: " + plateau.getPhaseActuelle() + ", Gagnant: " + plateau.getGagnant());
            out.writeObject(plateau);
            out.reset();  
            out.flush();
            System.out.println(" PLATEAU ENVOYÉ");
        } catch (IOException e) {
            System.err.println(" Erreur d'envoi plateau: " + e.getMessage());
        }
    }

    
    // Envoie le plateau courant du serveur au client
    public void envoyerPlateau() {
        envoyerPlateau(serveur.getPlateau());
    }

    
    // Envoie l'objet `Joueur` correspondant à ce client
    public void envoyerInfoJoueur() {
        try {
            out.writeObject(joueur);
            out.reset();
            out.flush();
        } catch (IOException e) {
            System.err.println(" Erreur d'envoi info joueur: " + e.getMessage());
        }
    }

    
    // Envoie un message texte au client (par ex. PONG)
    public void envoyerMessage(String message) {
        try {
            out.writeObject(message);
            out.reset();
            out.flush();
        } catch (IOException e) {
            System.err.println(" Erreur d'envoi message: " + e.getMessage());
        }
    }

    
    public void envoyer(Object obj) {
        try {
            out.writeObject(obj);
            out.reset();
            out.flush();
        } catch (IOException e) {
            System.err.println(" Erreur d'envoi: " + e.getMessage());
        }
    }

    
    // Envoie au client la liste des autres joueurs (sans lui-même)
    public void envoyerCartesAutresJoueurs() {
        try {
            
            java.util.List<Joueur> tousLesJoueurs = serveur.getTousLesJoueurs();
            
            
            java.util.List<Joueur> autresJoueurs = new java.util.ArrayList<>();
            for (Joueur j : tousLesJoueurs) {
                if (j.getId() != this.idJoueur) {
                    autresJoueurs.add(j);
                }
            }
            
            out.writeObject(autresJoueurs);
            out.reset();
            out.flush();
            
            System.out.println(" Cartes de " + autresJoueurs.size() + " autres joueur(s) envoyées au joueur " + idJoueur);
        } catch (IOException e) {
            System.err.println(" Erreur d'envoi cartes autres joueurs: " + e.getMessage());
        }
    }

    
    // Ferme proprement la connexion et les flux pour ce client
    public void fermer() {
        actif = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(" Erreur fermeture socket: " + e.getMessage());
        }
    }

    
    // Retourne l'ID joueur géré par ce handler
    public int getIdJoueur() {
        return idJoueur;
    }

    // Retourne l'objet Joueur associé à ce client
    public Joueur getJoueur() {
        return joueur;
    }

    // Associe un objet Joueur à ce client handler
    public void setJoueur(Joueur joueur) {
        this.joueur = joueur;
    }

    // Indique si la connexion du client est active
    public boolean isActif() {
        return actif && !socket.isClosed();
    }
}

