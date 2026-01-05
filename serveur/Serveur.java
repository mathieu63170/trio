package serveur;

import commun.action.Action;
import commun.action.ActionMax;
import commun.action.ActionMin;
import commun.action.ActionRevealCarte;
import commun.action.ActionTrio;
import commun.plateau.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Classe Serveur : gère les connexions clients et le déroulement du jeu
 * Utilise la sérialisation Java pour communiquer avec les clients
 */
public class Serveur {
    private final int port;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients;
    private Plateau plateau;
    private final Regle regle;
    private boolean jeuEnCours;
    private static final int MIN_JOUEURS = 3;
    private static final int MAX_JOUEURS = 6;
    private static final long DELAI_ATTENTE_MS = 30000;  // 30 secondes d'attente
    private long timestampPremierJoueur = -1;
    private Object verouJeu = new Object();
    private Deck deckGlobal;  // Deck unique pour la partie
    private Map<Integer, Integer> triosParTour = new HashMap<>();  // Compte le nombre de trios faits par joueur dans ce tour

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
            System.out.println("En attente de connexions des clients (MIN: " + MIN_JOUEURS + ", MAX: " + MAX_JOUEURS + ")...");

            // Initialiser le plateau
            initierPlateau();

            // Thread de surveillance pour lancer la partie
            new Thread(this::surveillerDemarrage).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("✓ Client connecté: " + clientSocket.getInetAddress());
                
                synchronized (verouJeu) {
                    // Rejeter si on a déjà trop de joueurs et la partie n'est pas lancée
                    if (clients.size() >= MAX_JOUEURS) {
                        System.out.println("❌ Serveur plein (" + MAX_JOUEURS + " joueurs max)");
                        clientSocket.close();
                        continue;
                    }
                    
                    int nextID = clients.size() + 1;
                    ClientHandler handler = new ClientHandler(this, clientSocket, nextID);
                    clients.add(handler);
                    new Thread(handler).start();
                    
                    System.out.println("✓ Joueur " + nextID + " enregistré (" + clients.size() + "/" + MAX_JOUEURS + ")");
                    
                    // Marquer le premier joueur
                    if (timestampPremierJoueur == -1) {
                        timestampPremierJoueur = System.currentTimeMillis();
                        System.out.println("⏱️  Attente de " + (DELAI_ATTENTE_MS / 1000) + "s ou " + MAX_JOUEURS + " joueurs...");
                    }
                    
                    // Lancer immédiatement si on a MAX_JOUEURS
                    if (clients.size() >= MAX_JOUEURS && !jeuEnCours) {
                        demarrerPartie();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("✗ Erreur serveur: " + e.getMessage());
        }
    }

    /**
     * Thread de surveillance - lance la partie après délai d'attente
     */
    private void surveillerDemarrage() {
        while (true) {
            try {
                Thread.sleep(1000);  // Vérifier chaque seconde
                
                synchronized (verouJeu) {
                    if (!jeuEnCours && timestampPremierJoueur != -1 && clients.size() > 0) {
                        long ecouled = System.currentTimeMillis() - timestampPremierJoueur;
                        
                        if (ecouled >= DELAI_ATTENTE_MS && clients.size() >= MIN_JOUEURS) {
                            System.out.println("⏱️  Délai d'attente écoulé. Lancement de la partie avec " + clients.size() + " joueurs...");
                            demarrerPartie();
                            timestampPremierJoueur = -1;
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void initierPlateau() {
        // Initialize empty plateau - will be filled at game start
        List<Joueur> joueurs = new ArrayList<>();
        List<Carte> millieu = new ArrayList<>();
        plateau = new Plateau(joueurs, millieu, 0, Phase.PREMIERE_MANCHE, -1);
    }

    private List<Carte> genererCartesCentre() {
        List<Carte> cartes = new ArrayList<>();
        Forme[] formes = {Forme.CERCLE, Forme.CARRE, Forme.ONDULATION};
        Couleur[] couleurs = {Couleur.ROUGE, Couleur.VERT, Couleur.VIOLET};
        Remplissage[] remplissages = {Remplissage.PLEIN, Remplissage.VIDE, Remplissage.RAYE};
        
        for (int i = 0; i < 9; i++) {
            int valeur = (i % 3) + 1;
            Forme forme = formes[i % 3];
            Couleur couleur = couleurs[(i / 3) % 3];
            Remplissage remplissage = remplissages[(i / 9) % 3];
            cartes.add(new Carte(valeur, forme, couleur, remplissage));
        }
        return cartes;
    }

    /**
     * Obtient le nombre de cartes au centre selon le nombre de joueurs
     */
    private int getCartesAuCentre(int nbJoueurs) {
        return switch(nbJoueurs) {
            case 3 -> 9;
            case 4 -> 8;
            case 5, 6 -> 6;
            default -> 9;
        };
    }
    
    /**
     * Obtient le nombre de cartes par joueur selon le nombre de joueurs
     */
    private int getCartesParJoueur(int nbJoueurs) {
        return switch(nbJoueurs) {
            case 3 -> 9;
            case 4 -> 7;
            case 5 -> 6;
            case 6 -> 5;
            default -> 9;
        };
    }

    private void demarrerPartie() {
        synchronized (verouJeu) {
            if (jeuEnCours) {
                return;  // Éviter les doubles lancements
            }
            
            jeuEnCours = true;
            int nbJoueurs = clients.size();
            System.out.println("🎮 ========================================");
            System.out.println("🎮 PARTIE LANCÉE avec " + nbJoueurs + " joueur(s)");
            System.out.println("🎮 ========================================");
            
            // Créer un deck unique mélangé pour toute la partie
            deckGlobal = new Deck();
            deckGlobal.melanger();
            System.out.println("📚 Deck mélangé avec 36 cartes (12 valeurs × 3 cartes)");
            
            int cartesParJoueur = getCartesParJoueur(nbJoueurs);
            int cartesAuCentre = getCartesAuCentre(nbJoueurs);
            
            // Créer les joueurs avec les bonnes cartes
            List<Joueur> joueurs = new ArrayList<>();
            for (ClientHandler client : clients) {
                List<Carte> deckJoueur = new ArrayList<>();
                for (int i = 0; i < cartesParJoueur; i++) {
                    Carte c = deckGlobal.tirerCarte();
                    if (c != null) deckJoueur.add(c);
                }
                Joueur joueur = new Joueur(client.getIdJoueur(), "Joueur " + client.getIdJoueur(), 
                                           deckJoueur, new ArrayList<>());
                joueurs.add(joueur);
                client.setJoueur(joueur);
                
                System.out.println("📊 Joueur " + client.getIdJoueur() + " : " + deckJoueur.size() + " cartes distribuées");
            }
            
            // Distribuer les cartes du centre
            List<Carte> milieu = new ArrayList<>();
            for (int i = 0; i < cartesAuCentre; i++) {
                Carte c = deckGlobal.tirerCarte();
                if (c != null) milieu.add(c);
            }
            System.out.println("🎯 Milieu : " + milieu.size() + " cartes");
            
            plateau.setJoueurs(joueurs);
            plateau.setMillieu(milieu);
            plateau.setJoueurActuel(clients.get(0).getIdJoueur());  // Commence par le premier joueur
            
            // Initialiser le compteur de trios par tour
            for (Joueur joueur : joueurs) {
                triosParTour.put(joueur.getId(), 0);
            }
            
            // Envoyer le plateau et les infos joueur à tous les clients
            for (ClientHandler client : clients) {
                if (client.isActif()) {
                    // Envoyer le plateau
                    client.envoyerPlateau(plateau);
                    
                    // Envoyer les infos du joueur
                    client.envoyerInfoJoueur();
                    
                    // Envoyer les autres joueurs
                    List<Joueur> autresJoueurs = new ArrayList<>();
                    for (Joueur j : joueurs) {
                        if (j.getId() != client.getIdJoueur()) {
                            autresJoueurs.add(j);
                        }
                    }
                    client.envoyer(autresJoueurs);
                }
            }
        }
    }

    /**
     * Traite une action reçue d'un client
     */
    public void traiterAction(ClientHandler client, Action action) {
        if (plateau == null) return;
        
        // Vérifier que c'est le tour du joueur
        if (action.getIdJoueur() != plateau.getJoueurActuel()) {
            System.out.println("   ❌ Ce n'est pas le tour du joueur " + action.getIdJoueur() + " (tour actuel: " + plateau.getJoueurActuel() + ")");
            return;
        }
        
        System.out.println("📋 Traitement action: " + action.getClass().getSimpleName() + " (étape " + (plateau.getEtapeJoueurActuel() + 1) + "/3)");
        
        // Traitement spécial pour MAX/MIN: révéler sans transférer
        if (action instanceof ActionMax) {
            ActionMax actMax = (ActionMax) action;
            // Incrémenter l'étape (les MIN/MAX comptent pour la limite)
            plateau.setEtapeJoueurActuel(plateau.getEtapeJoueurActuel() + 1);
            traiterActionMaxMin(actMax.getIdJoueur(), actMax.getIdCible(), "MAX");
            
            // NE PAS passer au joueur suivant automatiquement après 3 MIN/MAX
            // Laisser le joueur faire un TRIO ou faire d'autres MIN/MAX
            // Le tour se termine seulement quand le joueur fait un TRIO (valide ou invalide)
            return;
        } else if (action instanceof ActionMin) {
            ActionMin actMin = (ActionMin) action;
            // Incrémenter l'étape (les MIN/MAX comptent pour la limite)
            plateau.setEtapeJoueurActuel(plateau.getEtapeJoueurActuel() + 1);
            traiterActionMaxMin(actMin.getIdJoueur(), actMin.getIdCible(), "MIN");
            
            // NE PAS passer au joueur suivant automatiquement après 3 MIN/MAX
            // Laisser le joueur faire un TRIO ou faire d'autres MIN/MAX
            // Le tour se termine seulement quand le joueur fait un TRIO (valide ou invalide)
            return;
        }
        
        // Traitement spécial pour TRIO: vérifier d'abord avant d'appliquer
        if (action instanceof ActionTrio) {
            ActionTrio actionTrio = (ActionTrio) action;
            System.out.println("   📋 TRIO tentative - " + (actionTrio.getIdsCartes() != null ? actionTrio.getIdsCartes().size() : 0) + " cartes");
            
            // Afficher les IDs des cartes envoyées
            if (actionTrio.getIdsCartes() != null && !actionTrio.getIdsCartes().isEmpty()) {
                System.out.println("   📊 IDs des cartes du TRIO:");
                for (Integer idCarte : actionTrio.getIdsCartes()) {
                    System.out.println("      • ID: " + idCarte);
                }
            }
            
            // Incrémenter l'étape (le trio compte comme une action)
            plateau.setEtapeJoueurActuel(plateau.getEtapeJoueurActuel() + 1);
            
            boolean trioValide = verifierTrioValide(actionTrio);
            
            if (trioValide) {
                // Le trio est valide, l'appliquer
                System.out.println("   ✅ Trio VALIDE! Application...");
                plateau = regle.appliquer(plateau, action);
                
                // Incrémenter le compteur de trios pour ce joueur dans ce tour
                int triosFaits = triosParTour.getOrDefault(action.getIdJoueur(), 0);
                triosParTour.put(action.getIdJoueur(), triosFaits + 1);
                
                System.out.println("   ✅ TRIO VALIDE - Le joueur " + action.getIdJoueur() + " a fait " + (triosFaits + 1) + " trio(s) dans ce tour - IL REJOUE!");
                broadcastPlateau();
                
                // Réinitialiser les cartes révélées APRÈS le test du trio
                System.out.println("   🔄 Réinitialisation des cartes révélées après test de trio");
                plateau.getCartesRevelees().clear();
                
                // Réinitialiser l'étape du joueur pour qu'il puisse rejouer
                // Chaque trio donne le droit de rejouer (reset l'étape)
                plateau.setEtapeJoueurActuel(0);
                broadcastPlateau();
                // NE PAS passer au joueur suivant - le joueur a le droit de rejouer
                
            } else {
                // Le trio est invalide
                System.out.println("   ❌ TRIO INVALIDE - Passage au joueur suivant");
                
                // Réinitialiser les cartes révélées APRÈS le test du trio
                System.out.println("   🔄 Réinitialisation des cartes révélées après test de trio");
                plateau.getCartesRevelees().clear();
                
                // Passer au joueur suivant seulement si le trio était invalide
                passerAuJoueurSuivant();
            }
            return;
        }
        
        // Traitement normal pour les autres actions (MILIEU, etc)
        plateau = regle.appliquer(plateau, action);
        // Incrémenter l'étape
        plateau.setEtapeJoueurActuel(plateau.getEtapeJoueurActuel() + 1);
        broadcastPlateau();
        
        // Si 3 étapes atteintes, passer au joueur suivant
        if (plateau.getEtapeJoueurActuel() >= 3) {
            System.out.println("   🔄 3 étapes atteintes, passage au joueur suivant");
            passerAuJoueurSuivant();
        }
    }
    
    /**
     * Traite MAX et MIN: révèle une carte sans la transférer
     */
    private void traiterActionMaxMin(int idJoueur, int idCible, String type) {
        Joueur demandeur = findJoueur(idJoueur);
        Joueur cible = findJoueur(idCible);
        
        if (cible == null || cible.getDeck().isEmpty()) {
            System.out.println("   ❌ Joueur " + idCible + " n'a pas de cartes");
            return;
        }
        
        // Vérifier si la cible a déjà 3 cartes révélées
        int cartesReveleesDelaCible = compterCartesReveleesParJoueur(idCible);
        if (cartesReveleesDelaCible >= 3) {
            System.out.println("   ❌ Joueur " + idCible + " a déjà " + cartesReveleesDelaCible + " cartes révélées (max 3)");
            return;
        }
        
        // Obtenir les cartes non révélées de la cible
        List<Carte> cartesNonRevelees = new ArrayList<>();
        for (Carte c : cible.getDeck()) {
            // Vérifier si la carte n'est pas déjà révélée dans la liste des cartes révélées
            boolean dejaRevelee = false;
            for (CarteRevealee cr : plateau.getCartesRevelees()) {
                if (cr.getIdProprietaire() == idCible && cr.getCarte().getId() == c.getId()) {
                    dejaRevelee = true;
                    break;
                }
            }
            if (!dejaRevelee) {
                cartesNonRevelees.add(c);
            }
        }
        
        if (cartesNonRevelees.isEmpty()) {
            System.out.println("   ❌ Joueur " + idCible + " n'a pas d'autres cartes à révéler");
            return;
        }
        
        // Trouver la carte selon MAX ou MIN parmi les cartes non révélées
        Carte carte = cartesNonRevelees.get(0);
        for (Carte c : cartesNonRevelees) {
            if (type.equals("MAX") && c.getValeur() > carte.getValeur()) {
                carte = c;
            } else if (type.equals("MIN") && c.getValeur() < carte.getValeur()) {
                carte = c;
            }
        }
        
        System.out.println("   📸 Carte révélée: " + carte.getValeur() + " (" + type + ") du Joueur " + idCible);
        
        // Ajouter la carte au liste des cartes révélées publiquement
        CarteRevealee carteRev = new CarteRevealee(carte, idCible, type);
        plateau.getCartesRevelees().add(carteRev);
        
        // Marquer la carte comme révélée
        carte.setRevelee(true);
        
        // Envoyer la révélation au demandeur (optionnel, pour notification)
        ActionRevealCarte reveal = new ActionRevealCarte(idJoueur, idCible, carte, type);
        ClientHandler demandeurClient = findClientByJoueurId(idJoueur);
        if (demandeurClient != null) {
            demandeurClient.envoyer(reveal);
        }
        
        // Diffuser le plateau mis à jour à tous les clients
        broadcastPlateau();
    }
    
    /**
     * Trouve un client par son ID joueur
     */
    private ClientHandler findClientByJoueurId(int joueurId) {
        for (ClientHandler client : clients) {
            if (client.getIdJoueur() == joueurId) {
                return client;
            }
        }
        return null;
    }
    
    /**
     * Trouve un joueur par son ID
     */
    private Joueur findJoueur(int id) {
        if (plateau == null || plateau.getJoueurs() == null) return null;
        return plateau.getJoueurs().stream()
            .filter(j -> j.getId() == id)
            .findFirst()
            .orElse(null);
    }

    /**
     * Envoie le plateau à tous les clients
     */
    private void broadcastPlateau() {
        System.out.println("📡 BROADCAST PLATEAU - Phase: " + plateau.getPhaseActuelle() + ", Gagnant: " + plateau.getGagnant());
        for (ClientHandler client : clients) {
            if (client.isActif()) {
                client.envoyerPlateau(plateau);
            }
        }
    }

    /**
     * Vérifie si un trio est valide (3 cartes avec la même valeur ET existant réellement)
     * Utilise les IDs des cartes depuis l'action
     */
    private boolean verifierTrioValide(ActionTrio action) {
        if (action == null) {
            System.out.println("   ❌ ActionTrio NULL");
            return false;
        }
        
        List<Integer> idsCartes = action.getIdsCartes();
        List<Integer> proprietaires = action.getProprietaires();
        
        System.out.println("   🔍 Vérification trio: " + (idsCartes != null ? idsCartes.size() : "null") + " cartes, " + 
                          (proprietaires != null ? proprietaires.size() : "null") + " propriétaires");
        
        if (idsCartes == null || idsCartes.size() != 3) {
            System.out.println("   ❌ Pas exactement 3 cartes");
            return false;
        }
        
        if (proprietaires == null || proprietaires.size() != 3) {
            System.out.println("   ❌ Pas exactement 3 propriétaires");
            return false;
        }
        
        // ✅ CHERCHER LES CARTES PAR ID PARTOUT DANS LE PLATEAU
        // On ne fait confiance à aucun propriétaire envoyé par le client
        // car le plateau peut avoir changé depuis que le client a sélectionné les cartes
        List<Carte> cartesRecherchees = new ArrayList<>();
        for (int i = 0; i < idsCartes.size(); i++) {
            int idCarte = idsCartes.get(i);
            
            // Chercher la carte n'importe où dans le plateau
            Carte carteTrouvee = trouverCarteParIDPartout(idCarte);
            if (carteTrouvee == null) {
                System.out.println("   ❌ La carte ID " + idCarte + " n'existe plus dans le plateau");
                return false;
            }
            cartesRecherchees.add(carteTrouvee);
            System.out.println("     ✓ Carte ID " + idCarte + " trouvée - Valeur: " + carteTrouvee.getValeur());
        }
        
        // Vérifier que toutes les cartes ont la même valeur
        int valeur = cartesRecherchees.get(0).getValeur();
        System.out.println("   🔍 Valeur attendue: " + valeur);
        for (int i = 0; i < cartesRecherchees.size(); i++) {
            Carte carte = cartesRecherchees.get(i);
            if (carte.getValeur() != valeur) {
                System.out.println("   ❌ Carte ID " + carte.getId() + " a une valeur différente: " + carte.getValeur() + " au lieu de " + valeur);
                return false;
            }
        }
        
        System.out.println("   ✅ Trio VALIDE détecté!");
        return true;
    }
    
    /**
     * Trouve une carte par son ID et son propriétaire
     */
    private Carte trouverCarteParID(int idCarte, int proprietaire) {
        // Le milieu peut être encodé comme 0 ou -1
        if (proprietaire <= 0) {
            // Carte du milieu
            for (Carte c : plateau.getMillieu()) {
                if (c.getId() == idCarte) {
                    return c;
                }
            }
        } else {
            // Carte d'un joueur
            Joueur joueur = findJoueur(proprietaire);
            if (joueur != null && joueur.getDeck() != null) {
                for (Carte c : joueur.getDeck()) {
                    if (c.getId() == idCarte) {
                        return c;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Trouve une carte par son ID n'importe où dans le plateau
     * (dans le milieu OU dans n'importe quel joueur)
     * Utilisé pour valider les trios car le propriétaire peut avoir changé
     */
    private Carte trouverCarteParIDPartout(int idCarte) {
        // Chercher dans le milieu
        if (plateau.getMillieu() != null) {
            for (Carte c : plateau.getMillieu()) {
                if (c.getId() == idCarte) {
                    return c;
                }
            }
        }
        
        // Chercher dans tous les joueurs
        if (plateau.getJoueurs() != null) {
            for (Joueur joueur : plateau.getJoueurs()) {
                if (joueur.getDeck() != null) {
                    for (Carte c : joueur.getDeck()) {
                        if (c.getId() == idCarte) {
                            return c;
                        }
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Passe au joueur suivant
     */
    private void passerAuJoueurSuivant() {
        if (plateau == null || plateau.getJoueurs() == null || plateau.getJoueurs().isEmpty()) {
            return;
        }
        
        int joueurActuelIndex = -1;
        for (int i = 0; i < plateau.getJoueurs().size(); i++) {
            if (plateau.getJoueurs().get(i).getId() == plateau.getJoueurActuel()) {
                joueurActuelIndex = i;
                break;
            }
        }
        
        if (joueurActuelIndex >= 0) {
            int nextIndex = (joueurActuelIndex + 1) % plateau.getJoueurs().size();
            int nextJoueurId = plateau.getJoueurs().get(nextIndex).getId();
            plateau.setJoueurActuel(nextJoueurId);
            plateau.setEtapeJoueurActuel(0);  // Réinitialiser le compteur d'étapes
            
            // Réinitialiser le compteur de trios pour le nouveau joueur
            triosParTour.put(nextJoueurId, 0);
            
            System.out.println("🔄 Tour du joueur " + plateau.getJoueurActuel());
            broadcastPlateau();
        }
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    /**
     * Compte le nombre de cartes révélées pour un joueur donné
     */
    private int compterCartesReveleesParJoueur(int idJoueur) {
        int count = 0;
        if (plateau.getCartesRevelees() != null) {
            for (CarteRevealee cr : plateau.getCartesRevelees()) {
                if (cr.getIdProprietaire() == idJoueur) {
                    count++;
                }
            }
        }
        return count;
    }

    public List<Joueur> getTousLesJoueurs() {
        List<Joueur> joueurs = new ArrayList<>();
        for (ClientHandler client : clients) {
            if (client.getJoueur() != null) {
                joueurs.add(client.getJoueur());
            }
        }
        return joueurs;
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        Serveur serveur = new Serveur(port);
        serveur.demarrer();
    }
}
