package commun.plateau;

import java.io.Serializable;


public enum Couleur implements Serializable {
    ROUGE("", "Rouge"),
    VERT("🟢", "Vert"),
    VIOLET("🟣", "Violet");

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
