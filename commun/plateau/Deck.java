package commun.plateau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe Deck : gère le paquet de 81 cartes du jeu Trio
 * Génère automatiquement toutes les combinaisons possibles
 */
public class Deck implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Carte> cartes;
    private int index;

    /**
     * Constructeur : génère automatiquement les 81 cartes
     */
    public Deck() {
        this.cartes = new ArrayList<>();
        this.index = 0;
        genererCartes();
    }

    /**
     * Génère les cartes: 12 valeurs × 3 cartes = 36 cartes total
     */
    private void genererCartes() {
        // Générer 3 exemplaires de chaque valeur (1-12)
        for (int exemplaire = 0; exemplaire < 3; exemplaire++) {
            for (int valeur = 1; valeur <= 12; valeur++) {
                cartes.add(new Carte(valeur, Forme.CERCLE, Couleur.ROUGE, Remplissage.PLEIN));
            }
        }
    }

    /**
     * Mélange aléatoire du paquet
     */
    public void melanger() {
        Collections.shuffle(cartes);
        index = 0;
    }

    /**
     * Tire les 12 premières cartes pour le plateau
     */
    public List<Carte> tirerCartesInitiales() {
        List<Carte> cartesInitiales = new ArrayList<>();
        for (int i = 0; i < 12 && index < cartes.size(); i++) {
            cartesInitiales.add(cartes.get(index++));
        }
        return cartesInitiales;
    }

    /**
     * Tire une carte supplémentaire du deck
     */
    public Carte tirerCarte() {
        if (index < cartes.size()) {
            return cartes.get(index++);
        }
        return null;
    }

    /**
     * Réinitialise le deck
     */
    public void reinitialiser() {
        index = 0;
        melanger();
    }

    /**
     * Retourne le nombre de cartes restantes
     */
    public int getCartesRestantes() {
        return cartes.size() - index;
    }

    /**
     * Retourne toutes les cartes
     */
    public List<Carte> getCartes() {
        return cartes;
    }

    /**
     * Retourne le nombre total de cartes
     */
    public int getTaille() {
        return cartes.size();
    }
}
