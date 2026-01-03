package serveur;

import commun.action.Action;
import commun.plateau.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe Serveur : gère les connexions clients et le déroulement du jeu
 */
public class Serveur {
    private int port;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private Plateau plateau;
    private Regle regle;
    private boolean jeuEnCours;

    public Serveur(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        this.regle = new Regle();
        this.jeuEnCours = false;
    }

    public void demarrer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("✓ Serveur démarré sur le port " + port);
            System.out.println("En attente de connexions des clients...");

            // Initialiser le plateau avec des joueurs vides
            initierPlateau();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("✓ Client connecté: " + clientSocket.getInetAddress());
                
                ClientHandler handler = new ClientHandler(this, clientSocket, clients.size() + 1);
                clients.add(handler);
                new Thread(handler).start();

                // Démarrer le jeu s'il y a au moins 3 joueurs
                if (clients.size() >= 2 && !jeuEnCours) {
                    demarrerPartie();
                }
            }
        } catch (IOException e) {
            System.err.println("✗ Erreur serveur: " + e.getMessage());
        }
    }

    private void initierPlateau() {
        List<Joueur> joueurs = new ArrayList<>();
        List<Carte> millieu = genererCartesCentre();
        plateau = new Plateau(joueurs, millieu, 0, Phase.PREMIERE_MANCHE, -1);
    }

    private List<Carte> genererCartesCentre() {
        List<Carte> cartes = new ArrayList<>();
        Forme[] formes = {Forme.CERCLE, Forme.CARRE, Forme.ONDULATION};
        Couleur[] couleurs = {Couleur.ROUGE, Couleur.VERT, Couleur.VIOLET};
        Remplissage[] remplissages = {Remplissage.PLEIN, Remplissage.VIDE, Remplissage.RAYE};
        
        for (int i = 0; i < 12; i++) {
            int valeur = (i % 3) + 1;
            Forme forme = formes[i % 3];
            Couleur couleur = couleurs[(i / 3) % 3];
            Remplissage remplissage = remplissages[(i / 9) % 3];
            cartes.add(new Carte(valeur, forme, couleur, remplissage));
        }
        return cartes;
    }

    private void demarrerPartie() {
        jeuEnCours = true;
        System.out.println("✓ Partie démarrée avec " + clients.size() + " joueurs");
        
        // Créer les joueurs
        Forme[] formes = {Forme.CERCLE, Forme.CARRE, Forme.ONDULATION};
        Couleur[] couleurs = {Couleur.ROUGE, Couleur.VERT, Couleur.VIOLET};
        Remplissage[] remplissages = {Remplissage.PLEIN, Remplissage.VIDE, Remplissage.RAYE};
        
        for (int i = 0; i < clients.size(); i++) {
            List<Carte> deck = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                int idx = i * 3 + j;
                int valeur = (idx % 3) + 1;
                Forme forme = formes[idx % 3];
                Couleur couleur = couleurs[(idx / 3) % 3];
                Remplissage remplissage = remplissages[(idx / 9) % 3];
                deck.add(new Carte(valeur, forme, couleur, remplissage));
            }
            Joueur joueur = new Joueur(i + 1, "Joueur " + (i + 1), deck, new ArrayList<>());
            plateau.getJoueurs().add(joueur);
        }
        
        // Notifier tous les clients
        broadcast("START:" + plateau.getJoueurs().size());
    }

    public void traiterAction(Action action) {
        if (plateau != null) {
            plateau = regle.appliquer(plateau, action);
            broadcast("UPDATE:" + plateau.getJoueurActuel());
        }
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.envoyer(message);
        }
    }

    public void arreterClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("✓ Client déconnecté. Clients actifs: " + clients.size());
        if (clients.isEmpty()) {
            jeuEnCours = false;
        }
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        Serveur serveur = new Serveur(port);
        serveur.demarrer();
    }
}

/**
 * Classe ClientHandler : gère la communication avec un client
 */
class ClientHandler implements Runnable {
    private Serveur serveur;
    private Socket socket;
    private int idJoueur;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Serveur serveur, Socket socket, int idJoueur) {
        this.serveur = serveur;
        this.socket = socket;
        this.idJoueur = idJoueur;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("WELCOME:" + idJoueur);
            
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Message reçu du joueur " + idJoueur + ": " + message);
                traiterMessage(message);
            }
        } catch (IOException e) {
            System.err.println("✗ Erreur client " + idJoueur + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serveur.arreterClient(this);
        }
    }

    private void traiterMessage(String message) {
        if (message.startsWith("ACTION:")) {
            // TODO: Parser et traiter les actions
            String action = message.substring(7);
            System.out.println("Action du joueur " + idJoueur + ": " + action);
        }
    }

    public void envoyer(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
