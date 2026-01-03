package commun.plateau;

import java.io.Serializable;

/**
 * Énumération des formes possibles pour les cartes du jeu Trio
 */
public enum Forme implements Serializable {
    CERCLE("O", "Cercle"),
    CARRE("[]", "Carre"),
    ONDULATION("~", "Ondulation");

    private final String symbole;
    private final String nom;

    Forme(String symbole, String nom) {
        this.symbole = symbole;
        this.nom = nom;
    }

    public String getSymbole() {
        return symbole;
    }

    public String getNom() {
        return nom;
    }

    @Override
    public String toString() {
        return symbole + " " + nom;
    }
}
