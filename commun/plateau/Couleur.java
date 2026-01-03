package commun.plateau;

import java.io.Serializable;

/**
 * Énumération des couleurs possibles pour les cartes du jeu Trio
 */
public enum Couleur implements Serializable {
    ROUGE("[R]", "Rouge"),
    VERT("[V]", "Vert"),
    VIOLET("[P]", "Violet");

    private final String emoji;
    private final String nom;

    Couleur(String emoji, String nom) {
        this.emoji = emoji;
        this.nom = nom;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getNom() {
        return nom;
    }

    @Override
    public String toString() {
        return emoji + " " + nom;
    }
}
