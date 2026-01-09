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


// Serveur : accepte des clients et gère le déroulement des parties
public class Serveur {
    private final int port;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients;
    private Plateau plateau;
    private final Regle regle;
    private boolean jeuEnCours;
    private static final int MIN_JOUEURS = 3;
    private static final int MAX_JOUEURS = 6;
    private static final long DELAI_ATTENTE_MS = 30000;  
    private long timestampPremierJoueur = -1;
    private Object verouJeu = new Object();
    private Deck deckGlobal;  
    private Map<Integer, Integer> triosParTour = new HashMap<>();  

    public Serveur(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        this.regle = new Regle();
        this.jeuEnCours = false;
    }

    // Boucle principale : accepte les connexions et lance la partie quand possible
    public void demarrer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println(" Serveur démarré sur le port " + port);
            System.out.println("En attente de connexions des clients (MIN: " + MIN_JOUEURS + ", MAX: " + MAX_JOUEURS + ")...");

            
            initierPlateau();

            
            new Thread(this::surveillerDemarrage).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(" Client connecté: " + clientSocket.getInetAddress());
                
                synchronized (verouJeu) {
                    
                    if (clients.size() >= MAX_JOUEURS) {
                        System.out.println(" Serveur plein (" + MAX_JOUEURS + " joueurs max)");
                        clientSocket.close();
                        continue;
                    }
                    
                    int nextID = clients.size() + 1;
                    ClientHandler handler = new ClientHandler(this, clientSocket, nextID);
                    clients.add(handler);
                    new Thread(handler).start();
                    
                    System.out.println(" Joueur " + nextID + " enregistré (" + clients.size() + "/" + MAX_JOUEURS + ")");
                    
                    
                    if (timestampPremierJoueur == -1) {
                        timestampPremierJoueur = System.currentTimeMillis();
                        System.out.println("⏱️  Attente de " + (DELAI_ATTENTE_MS / 1000) + "s ou " + MAX_JOUEURS + " joueurs...");
                    }
                    
                    
                    if (clients.size() >= MAX_JOUEURS && !jeuEnCours) {
                        demarrerPartie();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(" Erreur serveur: " + e.getMessage());
        }
    }

    
    private void surveillerDemarrage() {
        while (true) {
            try {
                Thread.sleep(1000);  
                
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

    
    private int getCartesAuCentre(int nbJoueurs) {
        return switch(nbJoueurs) {
            case 3 -> 9;
            case 4 -> 8;
            case 5, 6 -> 6;
            default -> 9;
        };
    }
    
    
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
                return;  
            }
            
            jeuEnCours = true;
            int nbJoueurs = clients.size();
            System.out.println(" ========================================");
            System.out.println(" PARTIE LANCÉE avec " + nbJoueurs + " joueur(s)");
            System.out.println(" ========================================");
            
            
            deckGlobal = new Deck();
            deckGlobal.melanger();
            System.out.println(" Deck mélangé avec 36 cartes (12 valeurs × 3 cartes)");
            
            int cartesParJoueur = getCartesParJoueur(nbJoueurs);
            int cartesAuCentre = getCartesAuCentre(nbJoueurs);
            
            
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
                
                System.out.println(" Joueur " + client.getIdJoueur() + " : " + deckJoueur.size() + " cartes distribuées");
            }
            
            
            List<Carte> milieu = new ArrayList<>();
            for (int i = 0; i < cartesAuCentre; i++) {
                Carte c = deckGlobal.tirerCarte();
                if (c != null) milieu.add(c);
            }
            System.out.println(" Milieu : " + milieu.size() + " cartes");
            
            plateau.setJoueurs(joueurs);
            plateau.setMillieu(milieu);
            plateau.setJoueurActuel(clients.get(0).getIdJoueur());  
            
            
            for (Joueur joueur : joueurs) {
                triosParTour.put(joueur.getId(), 0);
            }
            
            
            for (ClientHandler client : clients) {
                if (client.isActif()) {
                    
                    client.envoyerPlateau(plateau);
                    
                    
                    client.envoyerInfoJoueur();
                    
                    
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

    
    public void traiterAction(ClientHandler client, Action action) {
        if (plateau == null) return;
        
        
        if (action.getIdJoueur() != plateau.getJoueurActuel()) {
            System.out.println("    Ce n'est pas le tour du joueur " + action.getIdJoueur() + " (tour actuel: " + plateau.getJoueurActuel() + ")");
            return;
        }
        
        System.out.println(" Traitement action: " + action.getClass().getSimpleName() + " (étape " + (plateau.getEtapeJoueurActuel() + 1) + "/3)");
        
        
        if (action instanceof ActionMax) {
            ActionMax actMax = (ActionMax) action;
            
            plateau.setEtapeJoueurActuel(plateau.getEtapeJoueurActuel() + 1);
            traiterActionMaxMin(actMax.getIdJoueur(), actMax.getIdCible(), "MAX");
            
            
            
            
            return;
        } else if (action instanceof ActionMin) {
            ActionMin actMin = (ActionMin) action;
            
            plateau.setEtapeJoueurActuel(plateau.getEtapeJoueurActuel() + 1);
            traiterActionMaxMin(actMin.getIdJoueur(), actMin.getIdCible(), "MIN");
            
            
            
            
            return;
        }
        
        
        if (action instanceof ActionTrio) {
            ActionTrio actionTrio = (ActionTrio) action;
            System.out.println("    TRIO tentative - " + (actionTrio.getIdsCartes() != null ? actionTrio.getIdsCartes().size() : 0) + " cartes");
            
            
            if (actionTrio.getIdsCartes() != null && !actionTrio.getIdsCartes().isEmpty()) {
                System.out.println("    IDs des cartes du TRIO:");
                for (Integer idCarte : actionTrio.getIdsCartes()) {
                    System.out.println("      • ID: " + idCarte);
                }
            }
            
            
            plateau.setEtapeJoueurActuel(plateau.getEtapeJoueurActuel() + 1);
            
            boolean trioValide = verifierTrioValide(actionTrio);
            
            if (trioValide) {
                
                System.out.println("    Trio VALIDE! Application...");
                plateau = regle.appliquer(plateau, action);
                
                
                int triosFaits = triosParTour.getOrDefault(action.getIdJoueur(), 0);
                triosParTour.put(action.getIdJoueur(), triosFaits + 1);
                
                System.out.println("    TRIO VALIDE - Le joueur " + action.getIdJoueur() + " a fait " + (triosFaits + 1) + " trio(s) dans ce tour - IL REJOUE!");
                broadcastPlateau();
                
                
                System.out.println("    Réinitialisation des cartes révélées après test de trio");
                plateau.getCartesRevelees().clear();
                
                
                
                plateau.setEtapeJoueurActuel(0);
                broadcastPlateau();
                
                
            } else {
                
                System.out.println("TRIO INVALIDE - Passage au joueur suivant");
                
                
                System.out.println("    Réinitialisation des cartes révélées après test de trio");
                plateau.getCartesRevelees().clear();
                
                
                passerAuJoueurSuivant();
            }
            return;
        } else if (action instanceof ActionRevealCarte) {
            ActionRevealCarte ar = (ActionRevealCarte) action;
            System.out.println("ActionRevealCarte reçue - demandeur: " + ar.getIdJoueur() + ", source: " + ar.getIdJoueurSource() + ", carte: " + (ar.getCarte()!=null ? ar.getCarte().getId() : "null"));
            traiterActionRevealCarte(ar);
            return;
        }
        
        
        plateau = regle.appliquer(plateau, action);
        
        plateau.setEtapeJoueurActuel(plateau.getEtapeJoueurActuel() + 1);
        broadcastPlateau();
        
        
        if (plateau.getEtapeJoueurActuel() >= 3) {
            System.out.println("    3 étapes atteintes, passage au joueur suivant");
            passerAuJoueurSuivant();
        }
    }
    
    
    private void traiterActionMaxMin(int idJoueur, int idCible, String type) {
        Joueur demandeur = findJoueur(idJoueur);
        Joueur cible = findJoueur(idCible);
        
        if (cible == null || cible.getDeck().isEmpty()) {
            System.out.println("    Joueur " + idCible + " n'a pas de cartes");
            return;
        }
        
        
        int cartesReveleesDelaCible = compterCartesReveleesParJoueur(idCible);
        if (cartesReveleesDelaCible >= 3) {
            System.out.println("    Joueur " + idCible + " a déjà " + cartesReveleesDelaCible + " cartes révélées (max 3)");
            return;
        }
        
        
        List<Carte> cartesNonRevelees = new ArrayList<>();
        for (Carte c : cible.getDeck()) {
            
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
            System.out.println("    Joueur " + idCible + " n'a pas d'autres cartes à révéler");
            return;
        }
        
        
        Carte carte = cartesNonRevelees.get(0);
        for (Carte c : cartesNonRevelees) {
            if (type.equals("MAX") && c.getValeur() > carte.getValeur()) {
                carte = c;
            } else if (type.equals("MIN") && c.getValeur() < carte.getValeur()) {
                carte = c;
            }
        }
        
        System.out.println("    Carte révélée: " + carte.getValeur() + " (" + type + ") du Joueur " + idCible);
        
        
        CarteRevealee carteRev = new CarteRevealee(carte, idCible, type);
        plateau.getCartesRevelees().add(carteRev);
        
        
        carte.setRevelee(true);
        
        
        ActionRevealCarte reveal = new ActionRevealCarte(idJoueur, idCible, carte, type);
        ClientHandler demandeurClient = findClientByJoueurId(idJoueur);
        if (demandeurClient != null) {
            demandeurClient.envoyer(reveal);
        }
        
        
        broadcastPlateau();
    }
    
    
    private void traiterActionRevealCarte(ActionRevealCarte ar) {
        int idSource = ar.getIdJoueurSource();
        int idDemandeur = ar.getIdJoueur();
        if (plateau == null) return;
        Carte carte = null;
        int ownerId = idDemandeur;

        if (idSource <= 0) {
            if (plateau.getMillieu() != null) {
                for (Carte c : plateau.getMillieu()) {
                    if (ar.getCarte() != null && c.getId() == ar.getCarte().getId()) {
                        carte = c;
                        break;
                    }
                }
            }
            if (carte == null) {
                System.out.println("Carte du milieu introuvable pour révélation");
                return;
            }
            ownerId = idDemandeur;

        } else {
            Joueur source = findJoueur(idSource);
            if (source == null || source.getDeck() == null) {
                System.out.println("Joueur source introuvable ou sans deck");
                return;
            }
            for (Carte c : source.getDeck()) {
                if (ar.getCarte() != null && c.getId() == ar.getCarte().getId()) {
                    carte = c;
                    break;
                }
            }
            if (carte == null) {
                System.out.println("Carte introuvable dans la main du joueur " + idSource);
                return;
            }
        }

        int cartesReveleesDuJoueur = compterCartesReveleesParJoueur(ownerId);
        if (cartesReveleesDuJoueur >= 3) {
            System.out.println("Le joueur " + ownerId + " a déjà " + cartesReveleesDuJoueur + " cartes révélées");
            return;
        }
        for (CarteRevealee cr : plateau.getCartesRevelees()) {
            if (cr.getIdProprietaire() == ownerId && cr.getCarte().getId() == carte.getId()) {
                System.out.println("Carte déjà révélée pour ce propriétaire");
                return;
            }
        }
        CarteRevealee carteRev = new CarteRevealee(carte, ownerId, ar.getType());
        plateau.getCartesRevelees().add(carteRev);
        carte.setRevelee(true);
        System.out.println("Carte révélée (source: " + idSource + ", owner: " + ownerId + ") : " + carte.getValeur());
        broadcastPlateau();
    }

    private ClientHandler findClientByJoueurId(int joueurId) {
        for (ClientHandler client : clients) {
            if (client.getIdJoueur() == joueurId) {
                return client;
            }
        }
        return null;
    }
    
    
    private Joueur findJoueur(int id) {
        if (plateau == null || plateau.getJoueurs() == null) return null;
        return plateau.getJoueurs().stream()
            .filter(j -> j.getId() == id)
            .findFirst()
            .orElse(null);
    }

    
    private void broadcastPlateau() {
        System.out.println("BROADCAST PLATEAU - Phase: " + plateau.getPhaseActuelle() + ", Gagnant: " + plateau.getGagnant());
        for (ClientHandler client : clients) {
            if (client.isActif()) {
                client.envoyerPlateau(plateau);
            }
        }
    }

    
    private boolean verifierTrioValide(ActionTrio action) {
        if (action == null) {
            System.out.println("ActionTrio NULL");
            return false;
        }
        
        List<Integer> idsCartes = action.getIdsCartes();
        List<Integer> proprietaires = action.getProprietaires();
        
        System.out.println("Vérification trio: " + (idsCartes != null ? idsCartes.size() : "null") + " cartes, " + 
                          (proprietaires != null ? proprietaires.size() : "null") + " propriétaires");
        
        if (idsCartes == null || idsCartes.size() != 3) {
            System.out.println("Pas exactement 3 cartes");
            return false;
        }
        
        if (proprietaires == null || proprietaires.size() != 3) {
            System.out.println("Pas exactement 3 propriétaires");
            return false;
        }
        
        
        
        
        List<Carte> cartesRecherchees = new ArrayList<>();
        for (int i = 0; i < idsCartes.size(); i++) {
            int idCarte = idsCartes.get(i);
            
            
            Carte carteTrouvee = trouverCarteParIDPartout(idCarte);
            if (carteTrouvee == null) {
                System.out.println("La carte ID " + idCarte + " n'existe plus dans le plateau");
                return false;
            }
            cartesRecherchees.add(carteTrouvee);
            System.out.println("Carte ID " + idCarte + " trouvée - Valeur: " + carteTrouvee.getValeur());
        }
        
        
        int valeur = cartesRecherchees.get(0).getValeur();
        System.out.println("    Valeur attendue: " + valeur);
        for (int i = 0; i < cartesRecherchees.size(); i++) {
            Carte carte = cartesRecherchees.get(i);
            if (carte.getValeur() != valeur) {
                System.out.println("    Carte ID " + carte.getId() + " a une valeur différente: " + carte.getValeur() + " au lieu de " + valeur);
                return false;
            }
        }
        
        System.out.println("    Trio VALIDE détecté!");
        return true;
    }
    
    
    private Carte trouverCarteParID(int idCarte, int proprietaire) {
        
        if (proprietaire <= 0) {
            
            for (Carte c : plateau.getMillieu()) {
                if (c.getId() == idCarte) {
                    return c;
                }
            }
        } else {
            
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
    
    
    private Carte trouverCarteParIDPartout(int idCarte) {
        
        if (plateau.getMillieu() != null) {
            for (Carte c : plateau.getMillieu()) {
                if (c.getId() == idCarte) {
                    return c;
                }
            }
        }
        
        
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
            plateau.setEtapeJoueurActuel(0);  
            
            
            triosParTour.put(nextJoueurId, 0);
            
            if (plateau.getMillieu() != null) {
                for (Carte c : plateau.getMillieu()) {
                    c.setRevelee(false);
                }
            }
            if (plateau.getCartesRevelees() != null) {
                plateau.getCartesRevelees().removeIf(cr -> "MILIEU".equals(cr.getTypeRevealation()));
            }
            System.out.println("Cartes du milieu cachées pour tous");

            System.out.println(" Tour du joueur " + plateau.getJoueurActuel());
            broadcastPlateau();
        }
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    
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
