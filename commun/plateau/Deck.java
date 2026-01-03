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
     * Génère les 81 cartes (3^4 combinaisons possibles)
     */
    private void genererCartes() {
        for (int valeur = 1; valeur <= 3; valeur++) {
            for (Forme forme : Forme.values()) {
                for (Couleur couleur : Couleur.values()) {
                    for (Remplissage remplissage : Remplissage.values()) {
                        cartes.add(new Carte(valeur, forme, couleur, remplissage));
                    }
                }
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
