package game;

import model.*;
import java.util.*;

/**
 * Classe représentant la logique du jeu Trio
 * Gère les règles, la validation des trios et la progression du jeu
 */
public class Trio {
    private Deck deck;
    private List<Carte> cartesEnJeu;
    private List<Joueur> joueurs;
    private Joueur joueurActuel;
    private List<Integer> cartesSelectionnees;
    private EtatJeu etat;

    public enum EtatJeu {
        ATTENTE, EN_COURS, TERMINE
    }

    private static final int CARTES_INITIALES = 12;

    /**
     * Constructeur du jeu Trio
     */
    public Trio(List<Joueur> joueurs) {
        this.joueurs = new ArrayList<>(joueurs);
        this.cartesSelectionnees = new ArrayList<>();
        this.deck = new Deck();
        this.cartesEnJeu = new ArrayList<>();
        this.etat = EtatJeu.ATTENTE;
        this.joueurActuel = null;
    }

    /**
     * Initialise une nouvelle partie
     */
    public void demarrerPartie() {
        if (joueurs.isEmpty()) {
            throw new IllegalStateException("Au moins un joueur est nécessaire");
        }

        deck.reinitialiser();
        cartesEnJeu.clear();
        cartesSelectionnees.clear();

        // Distribue les cartes initiales
        cartesEnJeu.addAll(deck.tirerCartes(CARTES_INITIALES));

        // Réinitialise les scores
        for (Joueur joueur : joueurs) {
            joueur.reinitialiserScore();
        }

        joueurActuel = joueurs.get(0);
        etat = EtatJeu.EN_COURS;
    }

    /**
     * Vérifie si trois cartes forment un trio valide
     * Règle : pour chaque attribut, soit les 3 valeurs sont identiques, soit elles sont toutes différentes
     */
    public boolean estTrioValide(Carte c1, Carte c2, Carte c3) {
        // Vérifie la valeur
        if (!verifierAttribut(c1.getValeur(), c2.getValeur(), c3.getValeur())) {
            return false;
        }

        // Vérifie la forme
        if (!verifierAttribut(c1.getForme(), c2.getForme(), c3.getForme())) {
            return false;
        }

        // Vérifie la couleur
        if (!verifierAttribut(c1.getCouleur(), c2.getCouleur(), c3.getCouleur())) {
            return false;
        }

        // Vérifie le remplissage
        if (!verifierAttribut(c1.getRemplissage(), c2.getRemplissage(), c3.getRemplissage())) {
            return false;
        }

        return true;
    }

    /**
     * Vérifie qu'un attribut est valide : soit tous identiques, soit tous différents
     */
    private <T> boolean verifierAttribut(T v1, T v2, T v3) {
        boolean sontIdentiques = v1.equals(v2) && v2.equals(v3);
        boolean sontDifferents = !v1.equals(v2) && !v2.equals(v3) && !v1.equals(v3);
        return sontIdentiques || sontDifferents;
    }

    /**
     * Sélectionne une carte (bascule sélection)
     */
    public void selectionnerCarte(int index) {
        if (index < 0 || index >= cartesEnJeu.size()) {
            throw new IllegalArgumentException("Index invalide");
        }

        if (cartesSelectionnees.contains(index)) {
            cartesSelectionnees.remove(Integer.valueOf(index));
        } else {
            if (cartesSelectionnees.size() < 3) {
                cartesSelectionnees.add(index);
            }
        }
    }

    /**
     * Vérifie et valide le trio sélectionné
     */
    public boolean verifierTrio() {
        if (cartesSelectionnees.size() != 3) {
            return false;
        }

        Carte c1 = cartesEnJeu.get(cartesSelectionnees.get(0));
        Carte c2 = cartesEnJeu.get(cartesSelectionnees.get(1));
        Carte c3 = cartesEnJeu.get(cartesSelectionnees.get(2));

        if (estTrioValide(c1, c2, c3)) {
            // Augmente le score
            joueurActuel.ajouterPoint();

            // Supprime les cartes (du plus grand indice au plus petit)
            cartesSelectionnees.sort((a, b) -> Integer.compare(b, a));
            for (int index : cartesSelectionnees) {
                cartesEnJeu.remove(index);
            }

            // Ajoute 3 nouvelles cartes si disponibles
            for (int i = 0; i < 3 && !deck.estVide(); i++) {
                cartesEnJeu.add(deck.tirerCarte());
            }

            cartesSelectionnees.clear();

            // Vérifie fin de partie
            if (cartesEnJeu.isEmpty() && deck.estVide()) {
                etat = EtatJeu.TERMINE;
            }

            return true;
        } else {
            cartesSelectionnees.clear();
            return false;
        }
    }

    /**
     * Passe au joueur suivant (utile en mode multijoueur)
     */
    public void joueurSuivant() {
        int index = joueurs.indexOf(joueurActuel);
        joueurActuel = joueurs.get((index + 1) % joueurs.size());
    }

    // Getters
    public List<Carte> getCartesEnJeu() {
        return new ArrayList<>(cartesEnJeu);
    }

    public List<Joueur> getJoueurs() {
        return new ArrayList<>(joueurs);
    }

    public Joueur getJoueurActuel() {
        return joueurActuel;
    }

    public List<Integer> getCartesSelectionnees() {
        return new ArrayList<>(cartesSelectionnees);
    }

    public int getNombreCartesRestantes() {
        return deck.getNombreCartesRestantes();
    }

    public EtatJeu getEtat() {
        return etat;
    }

    public boolean estTerminee() {
        return etat == EtatJeu.TERMINE;
    }

    @Override
    public String toString() {
        return "Trio [" + cartesEnJeu.size() + " cartes en jeu, " + joueurs.size() + " joueurs]";
    }
}
