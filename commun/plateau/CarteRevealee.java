package commun.plateau;

import java.io.Serializable;

/**
 * Représente une carte révélée publiquement (après MAX/MIN)
 * Contient la carte et l'ID du joueur propriétaire
 */
public class CarteRevealee implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Carte carte;
    private int idProprietaire;  // ID du joueur qui possède cette carte
    private String typeRevealation;  // "MAX", "MIN", etc.
    
    public CarteRevealee(Carte carte, int idProprietaire, String typeRevealation) {
        this.carte = carte;
        this.idProprietaire = idProprietaire;
        this.typeRevealation = typeRevealation;
    }
    
    // Getters
    public Carte getCarte() { return carte; }
    public int getIdProprietaire() { return idProprietaire; }
    public String getTypeRevealation() { return typeRevealation; }
    
    // Setters
    public void setCarte(Carte carte) { this.carte = carte; }
    public void setIdProprietaire(int idProprietaire) { this.idProprietaire = idProprietaire; }
    public void setTypeRevealation(String typeRevealation) { this.typeRevealation = typeRevealation; }
}
