package serveur;

import commun.action.*;
import commun.plateau.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatutJeu {

    private Plateau plateau;
    private final Regle regle;
    private final List<ObjectOutputStream> clientsOutput; 
    private final Object plateauLock = new Object(); 

    public StatutJeu() {
        this.regle = new Regle();
        this.clientsOutput = new ArrayList<>();
    }

    
    public void initialiserPartie(int nbJoueurs) {
        System.out.println("Initialisation de la partie pour " + nbJoueurs + " joueurs...");

        
        List<Carte> toutesLesCartes = new ArrayList<>();
        Forme[] formes = {Forme.CERCLE, Forme.CARRE, Forme.ONDULATION};
        Couleur[] couleurs = {Couleur.ROUGE, Couleur.VERT, Couleur.VIOLET};
        Remplissage[] remplissages = {Remplissage.PLEIN, Remplissage.VIDE, Remplissage.RAYE};
        
        for (int val = 1; val <= 12; val++) {
            for (int i = 0; i < 3; i++) {
                Forme forme = formes[(val - 1) % 3];
                Couleur couleur = couleurs[(val - 1) / 3 % 3];
                Remplissage remplissage = remplissages[(val - 1) / 9 % 3];
                toutesLesCartes.add(new Carte(1, forme, couleur, remplissage));
            }
        }

        
        Collections.shuffle(toutesLesCartes);

        
        List<Joueur> joueurs = new ArrayList<>();
        for (int i = 0; i < nbJoueurs; i++) {
            Joueur j = new Joueur(i, "Joueur " + i, new ArrayList<>(), new ArrayList<>());
            joueurs.add(j);
        }

        
        
        
        int cartesAuMilieu = 9; 
        int cartesParJoueur = (36 - cartesAuMilieu) / nbJoueurs;

        int indexCarte = 0;
        for (Joueur j : joueurs) {
            for (int k = 0; k < cartesParJoueur; k++) {
                j.getDeck().add(toutesLesCartes.get(indexCarte++));
            }
            
            
            
            Collections.sort(j.getDeck(), Comparator.comparingInt(Carte::getValeur));
        }

        
        List<Carte> milieu = new ArrayList<>();
        while (indexCarte < toutesLesCartes.size()) {
            milieu.add(toutesLesCartes.get(indexCarte++));
        }

        
        this.plateau = new Plateau(joueurs, milieu, 0, Phase.PREMIERE_MANCHE, -1);
        System.out.println("Partie initialisée ! Plateau prêt.");
    }

    
    public void demarrerServeur(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Serveur démarré sur le port " + port);

            
            initialiserPartie(2);

            int idJoueurConnecte = 0;

            while (idJoueurConnecte < 2) { 
                Socket socket = serverSocket.accept();
                System.out.println("Joueur " + idJoueurConnecte + " connecté !");

                
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                clientsOutput.add(out);
                
                
                out.writeObject(plateau);
                out.flush();

                
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                
                
                Thread t = new Thread(new GestionJoueur(in, idJoueurConnecte));
                t.start();

                idJoueurConnecte++;
            }
            
        } catch (IOException e) {
            System.err.println("Erreur serveur: " + e.getMessage());
        }
    }

    
    class GestionJoueur implements Runnable {
        private final ObjectInputStream in;
        private final int idJoueur;

        public GestionJoueur(ObjectInputStream in, int id) {
            this.in = in;
            this.idJoueur = id;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    
                    Object objetRecu = in.readObject();
                    
                    if (objetRecu instanceof Action action) {
                        System.out.println("Action reçue du J" + idJoueur);

                        
                        synchronized (StatutJeu.this.plateauLock) {
                            regle.appliquer(StatutJeu.this.plateau, action);
                        }

                        
                        diffuserPlateau();
                    }
                }
            } catch (IOException e) {
                System.err.println("Joueur " + idJoueur + " déconnecté: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("Erreur de désérialisation: " + e.getMessage());
            }
        }
    }

    private void diffuserPlateau() {
        for (ObjectOutputStream out : clientsOutput) {
            try {
                out.reset(); 
                out.writeObject(plateau);
                out.flush();
            } catch (IOException e) {
                System.err.println("Erreur lors de l'envoi du plateau: " + e.getMessage());
            }
        }
    }

    
    public static void main(String[] args) {
        StatutJeu serveur = new StatutJeu();
        serveur.demarrerServeur(12345);
    }
}