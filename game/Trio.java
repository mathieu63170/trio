package game;

import commun.plateau.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe Trio : gère la logique principale du jeu Trio
 */
public class Trio implements Serializable {
    private static final long serialVersionUID = 1L;

    private Deck deck;
    private Plateau plateau;
    private List<Joueur> joueurs;
    private int joueurActuel;
    private List<Integer> selection;
    private boolean jeuEnCours;
    private static final int NB_CARTES_PLATEAU = 12;

    /**
     * Constructeur
     */
    public Trio(List<Joueur> joueurs) {
        this.joueurs = joueurs;
        this.deck = new Deck();
        this.selection = new ArrayList<>();
        this.joueurActuel = 0;
        this.jeuEnCours = false;
    }

    /**
     * Démarre une nouvelle partie
     */
    public void demarrerPartie() {
        deck.melanger();
        List<Carte> cartesInitiales = deck.tirerCartesInitiales();
        
        // Initialiser le plateau
        plateau = new Plateau(joueurs, cartesInitiales, 0, Phase.PREMIERE_MANCHE, -1);
        
        // Distribuer des cartes aux joueurs (3 cartes chacun)
        for (Joueur joueur : joueurs) {
            List<Carte> deckJoueur = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Carte carte = deck.tirerCarte();
                if (carte != null) {
                    deckJoueur.add(carte);
                }
            }
            joueur.setDeck(deckJoueur);
        }

        selection.clear();
        joueurActuel = 0;
        jeuEnCours = true;
        System.out.println("✓ Partie démarrée avec " + joueurs.size() + " joueurs");
    }

    /**
     * Sélectionne une carte sur le plateau
     */
    public void selectionnerCarte(int index) {
        if (index >= 0 && index < plateau.getMillieu().size()) {
            if (selection.contains(index)) {
                selection.remove(Integer.valueOf(index));
                System.out.println("Carte " + index + " désélectionnée");
            } else if (selection.size() < 3) {
                selection.add(index);
                plateau.getMillieu().get(index).setRevelee(true);
                System.out.println("Carte " + index + " sélectionnée: " + plateau.getMillieu().get(index));
            }
        }
    }

    /**
     * Valide le trio sélectionné
     */
    public boolean validerTrio() {
        if (selection.size() != 3) {
            System.out.println("❌ Sélectionnez exactement 3 cartes!");
            return false;
        }

        Carte carte1 = plateau.getMillieu().get(selection.get(0));
        Carte carte2 = plateau.getMillieu().get(selection.get(1));
        Carte carte3 = plateau.getMillieu().get(selection.get(2));

        if (verifierTrio(carte1, carte2, carte3)) {
            System.out.println("✓ TRIO VALIDE!");
            
            // Ajouter le trio au joueur
            Joueur joueur = joueurs.get(joueurActuel);
            if (joueur.getTrios() == null) {
                joueur.setTrios(new ArrayList<>());
            }
            joueur.getTrios().add(new commun.plateau.Trio(new ArrayList<>(List.of(carte1, carte2, carte3)), 1));

            // Retirer les cartes et en ajouter 3 nouvelles
            List<Carte> millieu = plateau.getMillieu();
            for (int i = selection.size() - 1; i >= 0; i--) {
                int index = selection.get(i);
                Carte nouvelleCarte = deck.tirerCarte();
                if (nouvelleCarte != null) {
                    millieu.set(index, nouvelleCarte);
                } else {
                    millieu.remove(index);
                }
            }

            // Passer au joueur suivant
            joueurActuel = (joueurActuel + 1) % joueurs.size();
            selection.clear();
            return true;
        } else {
            System.out.println("❌ Trio invalide!");
            // Cacher les cartes
            for (int index : selection) {
                plateau.getMillieu().get(index).setRevelee(false);
            }
            selection.clear();
            
            // Passer au joueur suivant
            joueurActuel = (joueurActuel + 1) % joueurs.size();
            return false;
        }
    }

    /**
     * Vérifie si 3 cartes forment un trio valide
     * Pour chaque attribut : soit identique, soit tous différents
     */
    private boolean verifierTrio(Carte c1, Carte c2, Carte c3) {
        // Vérifier valeur
        if (!verifierAttribut(c1.getValeur(), c2.getValeur(), c3.getValeur())) {
            return false;
        }

        // Vérifier forme
        if (!verifierAttributForme(c1.getForme(), c2.getForme(), c3.getForme())) {
            return false;
        }

        // Vérifier couleur
        if (!verifierAttributCouleur(c1.getCouleur(), c2.getCouleur(), c3.getCouleur())) {
            return false;
        }

        // Vérifier remplissage
        if (!verifierAttributRemplissage(c1.getRemplissage(), c2.getRemplissage(), c3.getRemplissage())) {
            return false;
        }

        return true;
    }

    /**
     * Vérifie un attribut numérique (valeur)
     */
    private boolean verifierAttribut(int v1, int v2, int v3) {
        // Tous identiques
        if (v1 == v2 && v2 == v3) {
            return true;
        }
        // Tous différents
        if (v1 != v2 && v2 != v3 && v1 != v3) {
            return true;
        }
        return false;
    }

    /**
     * Vérifie l'attribut Forme
     */
    private boolean verifierAttributForme(Forme f1, Forme f2, Forme f3) {
        if (f1 == f2 && f2 == f3) {
            return true;
        }
        if (f1 != f2 && f2 != f3 && f1 != f3) {
            return true;
        }
        return false;
    }

    /**
     * Vérifie l'attribut Couleur
     */
    private boolean verifierAttributCouleur(Couleur c1, Couleur c2, Couleur c3) {
        if (c1 == c2 && c2 == c3) {
            return true;
        }
        if (c1 != c2 && c2 != c3 && c1 != c3) {
            return true;
        }
        return false;
    }

    /**
     * Vérifie l'attribut Remplissage
     */
    private boolean verifierAttributRemplissage(Remplissage r1, Remplissage r2, Remplissage r3) {
        if (r1 == r2 && r2 == r3) {
            return true;
        }
        if (r1 != r2 && r2 != r3 && r1 != r3) {
            return true;
        }
        return false;
    }

    /**
     * Annule la sélection
     */
    public void annulerSelection() {
        for (int index : selection) {
            plateau.getMillieu().get(index).setRevelee(false);
        }
        selection.clear();
    }

    // Getters et Setters
    public Plateau getPlateau() {
        return plateau;
    }

    public int getJoueurActuel() {
        return joueurActuel;
    }

    public Joueur getJoueurCourant() {
        return joueurs.get(joueurActuel);
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public List<Integer> getSelection() {
        return selection;
    }

    public boolean isJeuEnCours() {
        return jeuEnCours;
    }

    public int getGagnant() {
        if (joueurs.isEmpty()) return -1;
        
        Joueur gagnant = joueurs.get(0);
        for (Joueur joueur : joueurs) {
            if ((joueur.getTrios() != null ? joueur.getTrios().size() : 0) > 
                (gagnant.getTrios() != null ? gagnant.getTrios().size() : 0)) {
                gagnant = joueur;
            }
        }
        return gagnant.getId();
    }

    public Joueur getGagnantJoueur() {
        if (joueurs.isEmpty()) return null;
        
        Joueur gagnant = joueurs.get(0);
        for (Joueur joueur : joueurs) {
            if ((joueur.getTrios() != null ? joueur.getTrios().size() : 0) > 
                (gagnant.getTrios() != null ? gagnant.getTrios().size() : 0)) {
                gagnant = joueur;
            }
        }
        return gagnant;
    }
}
