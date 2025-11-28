package client;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Client Trio - Interface client pour se connecter au serveur
 * Responsable de:
 * - Connexion au serveur
 * - Envoi des mouvements du joueur
 * - Réception des mises à jour du jeu
 * - Affichage de l'interface utilisateur
 */
public class TrioClient {
    
    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nomJoueur;
    private boolean connecte;
    private TrioClientGUI gui;
    
    /**
     * Constructeur du client
     * @param host Adresse du serveur
     * @param port Port du serveur
     */
    public TrioClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.connecte = false;
    }
    
    /**
     * Se connecter au serveur
     */
    public boolean connecter(String nomJoueur) {
        try {
            this.nomJoueur = nomJoueur;
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Envoyer le nom du joueur
            out.println("JOIN:" + nomJoueur);
            
            connecte = true;
            System.out.println("[CLIENT] Connecté au serveur " + host + ":" + port);
            
            // Démarrer thread de réception
            Thread threadEcoute = new Thread(new Runnable() {
                public void run() {
                    ecouterServeur();
                }
            });
            threadEcoute.start();
            
            return true;
        } catch (IOException e) {
            System.err.println("[CLIENT] Erreur de connexion: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoyer une action au serveur
     */
    public void envoyerAction(String action) {
        if (connecte && out != null) {
            out.println(action);
        }
    }
    
    /**
     * Sélectionner une carte
     */
    public void selectionnerCarte(int index) {
        envoyerAction("SELECT:" + index);
    }
    
    /**
     * Vérifier le trio
     */
    public void verifierTrio() {
        envoyerAction("VERIFY");
    }
    
    /**
     * Annuler la sélection
     */
    public void annulerSelection() {
        envoyerAction("CANCEL");
    }
    
    /**
     * Nouvelle partie
     */
    public void nouvellePartie() {
        envoyerAction("NEW_GAME");
    }
    
    /**
     * Écouter les messages du serveur
     */
    private void ecouterServeur() {
        try {
            String message;
            while (connecte && (message = in.readLine()) != null) {
                traiterMessage(message);
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Perte de connexion: " + e.getMessage());
            connecte = false;
        }
    }
    
    /**
     * Traiter un message du serveur
     */
    private void traiterMessage(String message) {
        String[] parts = message.split(":", 2);
        String commande = parts[0];
        String data = parts.length > 1 ? parts[1] : "";
        
        switch (commande) {
            case "GAME_STATE":
                mettreAJourEtatJeu(data);
                break;
            case "CARDS":
                afficherCartes(data);
                break;
            case "SCORES":
                afficherScores(data);
                break;
            case "MESSAGE":
                afficherMessage(data);
                break;
            case "GAME_OVER":
                afficherGagnant(data);
                break;
            case "ERROR":
                afficherErreur(data);
                break;
            default:
                System.out.println("[SERVER] " + message);
        }
    }
    
    private void mettreAJourEtatJeu(String data) {
        if (gui != null) {
            gui.mettreAJourEtat(data);
        }
    }
    
    private void afficherCartes(String data) {
        if (gui != null) {
            gui.afficherCartes(data);
        }
    }
    
    private void afficherScores(String data) {
        if (gui != null) {
            gui.afficherScores(data);
        }
    }
    
    private void afficherMessage(String data) {
        System.out.println("[MESSAGE] " + data);
        if (gui != null) {
            gui.afficherMessage(data);
        }
    }
    
    private void afficherGagnant(String data) {
        System.out.println("[GAME OVER] " + data);
        if (gui != null) {
            gui.afficherGagnant(data);
        }
    }
    
    private void afficherErreur(String data) {
        System.err.println("[ERREUR] " + data);
    }
    
    /**
     * Définir l'interface graphique
     */
    public void setGUI(TrioClientGUI gui) {
        this.gui = gui;
    }
    
    /**
     * Déconnecter du serveur
     */
    public void deconnecter() {
        try {
            connecte = false;
            if (socket != null) socket.close();
            System.out.println("[CLIENT] Déconnecté du serveur");
        } catch (IOException e) {
            System.err.println("[CLIENT] Erreur de déconnexion: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;
        
        TrioClient client = new TrioClient(host, port);
        new TrioClientGUI(client);
    }
}
