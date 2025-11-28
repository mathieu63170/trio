package model;

/**
 * Énumération des remplissages de cartes dans le jeu Trio
 */
public enum Remplissage {
    PLEIN("Plein"),
    VIDE("Vide"),
    RAYES("Rayé");

    private final String nom;

    Remplissage(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    @Override
    public String toString() {
        return nom;
    }
}
