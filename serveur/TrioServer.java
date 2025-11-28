package serveur;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Serveur Trio - Gère les connexions et la logique du jeu
 * Responsable de:
 * - Accepter les connexions des clients
 * - Gérer les joueurs
 * - Exécuter la logique du jeu
 * - Diffuser les mises à jour aux clients
 */
public class TrioServer {
    
    private static final int PORT = 5000;
    private static final int MAX_JOUEURS = 4;
    
    private ServerSocket serverSocket;
    private TrioPartieServeur partie;
    private Map<String, ClientHandler> clients;
    private ExecutorService threadPool;
    
    /**
     * Constructeur du serveur
     */
    public TrioServer() {
        this.clients = new ConcurrentHashMap<>();
        this.threadPool = Executors.newFixedThreadPool(MAX_JOUEURS);
    }
    
    /**
     * Démarrer le serveur
     */
    public void demarrer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("[SERVEUR] Démarré sur le port " + PORT);
            System.out.println("[SERVEUR] En attente de connexions...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(this, clientSocket);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur: " + e.getMessage());
        } finally {
            arreter();
        }
    }
    
    /**
     * Ajouter un client au serveur
     */
    public synchronized void ajouterClient(String nomJoueur, ClientHandler handler) {
        clients.put(nomJoueur, handler);
        System.out.println("[SERVEUR] " + nomJoueur + " connecté. Clients: " + clients.size());
        
        // Si 2+ joueurs, démarrer la partie
        if (clients.size() == 2 && partie == null) {
            demarrerPartie();
        }
    }
    
    /**
     * Retirer un client du serveur
     */
    public synchronized void retirerClient(String nomJoueur) {
        clients.remove(nomJoueur);
        System.out.println("[SERVEUR] " + nomJoueur + " déconnecté. Clients: " + clients.size());
    }
    
    /**
     * Démarrer une nouvelle partie
     */
    private synchronized void demarrerPartie() {
        List<String> nomJoueurs = new ArrayList<>(clients.keySet());
        partie = new TrioPartieServeur(nomJoueurs);
        
        System.out.println("[SERVEUR] Partie démarrée avec " + nomJoueurs.size() + " joueurs");
        
        // Envoyer l'état initial
        diffuserEtatJeu();
    }
    
    /**
     * Diffuser l'état du jeu à tous les clients
     */
    public synchronized void diffuserEtatJeu() {
        if (partie != null) {
            String etat = partie.getEtat();
            for (ClientHandler client : clients.values()) {
                client.envoyer("GAME_STATE:" + etat);
            }
        }
    }
    
    /**
     * Obtenir la partie courante
     */
    public TrioPartieServeur getPartie() {
        return partie;
    }
    
    /**
     * Obtenir les clients connectés
     */
    public Map<String, ClientHandler> getClients() {
        return clients;
    }
    
    /**
     * Arrêter le serveur
     */
    public void arreter() {
        try {
            if (serverSocket != null) serverSocket.close();
            threadPool.shutdown();
            System.out.println("[SERVEUR] Arrêté");
        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur d'arrêt: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        TrioServer serveur = new TrioServer();
        serveur.demarrer();
    }
}
