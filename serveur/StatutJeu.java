package serveur;

import commun.plateau.*;
import commun.action.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatutJeu {

    private Plateau plateau;
    private Regle regle;
    private List<ObjectOutputStream> clientsOutput; // Pour envoyer les infos aux joueurs

    public StatutJeu() {
        this.regle = new Regle();
        this.clientsOutput = new ArrayList<>();
    }

    /**
     * Prépare le jeu : Crée les cartes, distribue et TRIE les mains.
     */
    public void initialiserPartie(int nbJoueurs) {
        System.out.println("Initialisation de la partie pour " + nbJoueurs + " joueurs...");

        // 1. Création du paquet (3 cartes de chaque valeur de 1 à 12)
        List<Carte> toutesLesCartes = new ArrayList<>();
        for (int val = 1; val <= 12; val++) {
            for (int i = 0; i < 3; i++) {
                // On pourrait ajouter un ID unique ici si Carte a un champ id
                toutesLesCartes.add(new Carte(val)); 
            }
        }

        // 2. Mélange
        Collections.shuffle(toutesLesCartes);

        // 3. Création des joueurs
        List<Joueur> joueurs = new ArrayList<>();
        for (int i = 0; i < nbJoueurs; i++) {
            Joueur j = new Joueur();
            j.setId(i); // ID simple : 0, 1, 2...
            j.setNom("Joueur " + i);
            j.setDeck(new ArrayList<>());
            j.setTrios(new ArrayList<>());
            joueurs.add(j);
        }

        // 4. Distribution
        // Règle standard Trio (selon nb joueurs, ici version simplifiée : tout sauf 9 cartes au milieu)
        // Disons qu'on laisse ~9 cartes au milieu et on distribue le reste équitablement
        int cartesAuMilieu = 9; // Variable selon règles exactes
        int cartesParJoueur = (36 - cartesAuMilieu) / nbJoueurs;

        int indexCarte = 0;
        for (Joueur j : joueurs) {
            for (int k = 0; k < cartesParJoueur; k++) {
                j.getDeck().add(toutesLesCartes.get(indexCarte++));
            }
            
            // --- IMPORTANT : TRIER LA MAIN DU JOUEUR ---
            // Les cartes doivent être triées du plus petit au plus grand pour que Min/Max fonctionne
            Collections.sort(j.getDeck(), Comparator.comparingInt(Carte::getValeur));
        }

        // 5. Cartes restantes au milieu
        List<Carte> milieu = new ArrayList<>();
        while (indexCarte < toutesLesCartes.size()) {
            milieu.add(toutesLesCartes.get(indexCarte++));
        }

        // 6. Création du Plateau initial
        this.plateau = new Plateau(joueurs, milieu, 0, Phase.EN_COURS, -1);
        System.out.println("Partie initialisée ! Plateau prêt.");
    }

    /**
     * Démarre le serveur et attend les connexions
     */
    public void demarrerServeur(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Serveur démarré sur le port " + port);

            // On initialise une partie à 2 joueurs pour tester (à adapter)
            initialiserPartie(2);

            int idJoueurConnecte = 0;

            while (idJoueurConnecte < 2) { // On attend 2 joueurs
                Socket socket = serverSocket.accept();
                System.out.println("Joueur " + idJoueurConnecte + " connecté !");

                // Flux de sortie (pour envoyer le plateau)
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                clientsOutput.add(out);
                
                // Envoyer le plateau initial tout de suite
                out.writeObject(plateau);
                out.flush();

                // Flux d'entrée (pour recevoir les actions) dans un Thread séparé
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                
                // Lancer le thread de gestion du joueur
                Thread t = new Thread(new GestionJoueur(in, idJoueurConnecte));
                t.start();

                idJoueurConnecte++;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Classe interne (Thread) pour écouter un joueur spécifique
     */
    class GestionJoueur implements Runnable {
        private ObjectInputStream in;
        private int idJoueur;

        public GestionJoueur(ObjectInputStream in, int id) {
            this.in = in;
            this.idJoueur = id;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // 1. Attendre une Action
                    Object objetRecu = in.readObject();
                    
                    if (objetRecu instanceof Action) {
                        Action action = (Action) objetRecu;
                        System.out.println("Action reçue du J" + idJoueur);

                        // 2. Appliquer les règles (Synchronized pour éviter conflits si 2 jouent en même temps)
                        synchronized (plateau) {
                            plateau = regle.appliquer(plateau, action);
                        }

                        // 3. Diffuser le nouveau plateau à TOUS les joueurs
                        diffuserPlateau();
                    }
                }
            } catch (Exception e) {
                System.out.println("Joueur " + idJoueur + " déconnecté.");
            }
        }
    }

    private void diffuserPlateau() {
        for (ObjectOutputStream out : clientsOutput) {
            try {
                out.reset(); // Indispensable pour rafraîchir l'objet envoyé
                out.writeObject(plateau);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // MAIN pour lancer le serveur
    public static void main(String[] args) {
        StatutJeu serveur = new StatutJeu();
        serveur.demarrerServeur(12345);
    }
}