package model;

/**
 * Classe représentant un joueur du jeu Trio
 */
public class Joueur {
    private String id;
    private String nom;
    private int score;
    private boolean actif;

    /**
     * Constructeur d'un joueur
     */
    public Joueur(String id, String nom) {
        this.id = id;
        this.nom = nom;
        this.score = 0;
        this.actif = true;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public int getScore() {
        return score;
    }

    public boolean isActif() {
        return actif;
    }

    // Setters
    public void setActif(boolean actif) {
        this.actif = actif;
    }

    /**
     * Augmente le score du joueur d'un point
     */
    public void ajouterPoint() {
        this.score++;
    }

    /**
     * Ajoute des points au joueur
     */
    public void ajouterPoints(int points) {
        this.score += points;
    }

    /**
     * Réinitialise le score du joueur
     */
    public void reinitialiserScore() {
        this.score = 0;
    }

    @Override
    public String toString() {
        return nom + " (Score: " + score + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Joueur)) return false;
        Joueur autre = (Joueur) obj;
        return id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
