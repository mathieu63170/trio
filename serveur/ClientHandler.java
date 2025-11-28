package serveur;

import java.io.*;
import java.net.*;

/**
 * ClientHandler - Gère la communication avec un client
 */
public class ClientHandler implements Runnable {
    
    private TrioServer server;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nomJoueur;
    private boolean connecte;
    
    /**
     * Constructeur
     */
    public ClientHandler(TrioServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.connecte = false;
    }
    
    /**
     * Exécution du handler
     */
    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Lire le nom du joueur
            String firstMessage = in.readLine();
            if (firstMessage != null && firstMessage.startsWith("JOIN:")) {
                nomJoueur = firstMessage.substring(5);
                connecte = true;
                server.ajouterClient(nomJoueur, this);
                envoyer("CONNECTED:Bienvenue " + nomJoueur);
            }
            
            // Boucle de réception des messages
            String message;
            while (connecte && (message = in.readLine()) != null) {
                traiterCommande(message);
            }
            
        } catch (IOException e) {
            System.err.println("[HANDLER] Erreur: " + e.getMessage());
        } finally {
            fermer();
        }
    }
    
    /**
     * Traiter une commande du client
     */
    private void traiterCommande(String commande) {
        String[] parts = commande.split(":", 2);
        String action = parts[0];
        String data = parts.length > 1 ? parts[1] : "";
        
        switch (action) {
            case "SELECT":
                traiterSelection(Integer.parseInt(data));
                break;
            case "VERIFY":
                traiterVerification();
                break;
            case "CANCEL":
                traiterAnnulation();
                break;
            case "NEW_GAME":
                traiterNouvellePartie();
                break;
            default:
                System.out.println("[HANDLER] Commande inconnue: " + action);
        }
    }
    
    private void traiterSelection(int index) {
        TrioPartieServeur partie = server.getPartie();
        if (partie != null) {
            partie.selectionnerCarte(nomJoueur, index);
            server.diffuserEtatJeu();
        }
    }
    
    private void traiterVerification() {
        TrioPartieServeur partie = server.getPartie();
        if (partie != null) {
            partie.verifierTrio(nomJoueur);
            server.diffuserEtatJeu();
        }
    }
    
    private void traiterAnnulation() {
        TrioPartieServeur partie = server.getPartie();
        if (partie != null) {
            partie.annulerSelection(nomJoueur);
            server.diffuserEtatJeu();
        }
    }
    
    private void traiterNouvellePartie() {
        // À implémenter
    }
    
    /**
     * Envoyer un message au client
     */
    public void envoyer(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    /**
     * Fermer la connexion
     */
    private void fermer() {
        try {
            connecte = false;
            if (nomJoueur != null) {
                server.retirerClient(nomJoueur);
            }
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("[HANDLER] Erreur de fermeture: " + e.getMessage());
        }
    }
}
