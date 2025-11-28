package model;

import java.util.*;

/**
 * Classe représentant le paquet de cartes du jeu Trio
 * Contient les 81 cartes (3 valeurs × 3 formes × 3 couleurs × 3 remplissages)
 */
public class Deck {
    private List<Carte> cartes;
    private static final int TOTAL_CARTES = 81;

    /**
     * Constructeur : initialise et mélange le deck
     */
    public Deck() {
        this.cartes = new ArrayList<>();
        genererCartes();
        melangerCartes();
    }

    /**
     * Génère les 81 cartes du jeu
     */
    private void genererCartes() {
        int id = 0;
        for (int valeur = 1; valeur <= 3; valeur++) {
            for (Forme forme : Forme.values()) {
                for (Couleur couleur : Couleur.values()) {
                    for (Remplissage remplissage : Remplissage.values()) {
                        cartes.add(new Carte(id++, valeur, forme, couleur, remplissage));
                    }
                }
            }
        }
    }

    /**
     * Mélange aléatoirement les cartes
     */
    public void melangerCartes() {
        Collections.shuffle(cartes);
    }

    /**
     * Tire et retourne une carte du deck
     */
    public Carte tirerCarte() {
        if (cartes.isEmpty()) {
            return null;
        }
        return cartes.remove(0);
    }

    /**
     * Tire et retourne n cartes du deck
     */
    public List<Carte> tirerCartes(int nombre) {
        List<Carte> cartesRecherchees = new ArrayList<>();
        for (int i = 0; i < nombre && !cartes.isEmpty(); i++) {
            cartesRecherchees.add(tirerCarte());
        }
        return cartesRecherchees;
    }

    /**
     * Retourne le nombre de cartes restantes
     */
    public int getNombreCartesRestantes() {
        return cartes.size();
    }

    /**
     * Vérifie si le deck est vide
     */
    public boolean estVide() {
        return cartes.isEmpty();
    }

    /**
     * Retourne toutes les cartes du deck (copie)
     */
    public List<Carte> getCartes() {
        return new ArrayList<>(cartes);
    }

    /**
     * Réinitialise le deck
     */
    public void reinitialiser() {
        cartes.clear();
        genererCartes();
        melangerCartes();
    }

    @Override
    public String toString() {
        return "Deck [" + cartes.size() + "/" + TOTAL_CARTES + " cartes restantes]";
    }
}
