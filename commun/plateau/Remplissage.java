package commun.plateau;

import java.io.Serializable;

/**
 * Énumération des types de remplissage possibles pour les cartes du jeu Trio
 */
public enum Remplissage implements Serializable {
    PLEIN("Plein", "##"),
    VIDE("Vide", "--"),
    RAYE("Raye", "%%");

    private final String nom;
    private final String symbole;

    Remplissage(String nom, String symbole) {
        this.nom = nom;
        this.symbole = symbole;
    }

    public String getNom() {
        return nom;
    }

    public String getSymbole() {
        return symbole;
    }

    @Override
    public String toString() {
        return symbole + " " + nom;
    }
}
