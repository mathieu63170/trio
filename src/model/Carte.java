package model;

/**
 * Classe représentant une carte du jeu Trio
 * Une carte est définie par 4 attributs :
 * - Valeur : nombre de symboles (1, 2 ou 3)
 * - Forme : type de symbole (Cercle, Carré, Ondulation)
 * - Couleur : couleur du symbole (Rouge, Vert, Violet)
 * - Remplissage : type de remplissage (Plein, Vide, Rayé)
 */
public class Carte {
    private int id;
    private int valeur;
    private Forme forme;
    private Couleur couleur;
    private Remplissage remplissage;

    /**
     * Constructeur d'une carte
     */
    public Carte(int id, int valeur, Forme forme, Couleur couleur, Remplissage remplissage) {
        this.id = id;
        this.valeur = valeur;
        this.forme = forme;
        this.couleur = couleur;
        this.remplissage = remplissage;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getValeur() {
        return valeur;
    }

    public Forme getForme() {
        return forme;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public Remplissage getRemplissage() {
        return remplissage;
    }

    /**
     * Affichage console de la carte
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ID:").append(String.format("%2d", id)).append("] ");
        for (int i = 0; i < valeur; i++) {
            sb.append(couleur.getEmoji()).append(forme.getSymbole());
        }
        sb.append(" ").append(couleur).append(" ").append(forme).append(" ").append(remplissage);
        return sb.toString();
    }

    /**
     * Affichage compact de la carte
     */
    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valeur; i++) {
            sb.append(forme.getSymbole());
        }
        return sb.toString();
    }
}
