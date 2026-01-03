package commun.plateau;
import java.io.Serializable;

/**
 * Classe Carte : représente une unique carte du jeu Trio
 * Une carte possède 4 attributs : valeur, forme, couleur, remplissage
 */
public class Carte implements Serializable {
    private static final long serialVersionUID = 1L; 
    
    private int valeur;              // 1, 2 ou 3 (nombre de symboles)
    private Forme forme;             // CERCLE, CARRE, ONDULATION
    private Couleur couleur;         // ROUGE, VERT, VIOLET
    private Remplissage remplissage; // PLEIN, VIDE, RAYE
    private boolean revelee;         // Si la carte est révélée

    /**
     * Constructeur complet
     */
    public Carte(int valeur, Forme forme, Couleur couleur, Remplissage remplissage) {
        this.valeur = valeur;
        this.forme = forme;
        this.couleur = couleur;
        this.remplissage = remplissage;
        this.revelee = false;
    }

    // Getters et Setters
    public int getValeur() {
        return valeur;
    }

    public void setValeur(int valeur) {
        this.valeur = valeur;
    }

    public Forme getForme() {
        return forme;
    }

    public void setForme(Forme forme) {
        this.forme = forme;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public void setCouleur(Couleur couleur) {
        this.couleur = couleur;
    }

    public Remplissage getRemplissage() {
        return remplissage;
    }

    public void setRemplissage(Remplissage remplissage) {
        this.remplissage = remplissage;
    }

    public boolean isRevelee() {
        return revelee;
    }

    public void setRevelee(boolean revelee) {
        this.revelee = revelee;
    }

    /**
     * Retourne une représentation textuelle de la carte
     */
    @Override
    public String toString() {
        return valeur + " " + forme.getSymbole() + " " + couleur.getEmoji() + " " + remplissage.getSymbole();
    }

    /**
     * Affichage détaillé
     */
    public String getDetails() {
        return valeur + " " + forme.getNom() + " " + couleur.getNom() + " " + remplissage.getNom();
    }
}
