package commun.plateau;

import java.io.Serializable;


public class CarteRevealee implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Carte carte;
    private int idProprietaire;  
    private String typeRevealation;  
    
    public CarteRevealee(Carte carte, int idProprietaire, String typeRevealation) {
        this.carte = carte;
        this.idProprietaire = idProprietaire;
        this.typeRevealation = typeRevealation;
    }
    
    
    public Carte getCarte() { return carte; }
    public int getIdProprietaire() { return idProprietaire; }
    public String getTypeRevealation() { return typeRevealation; }
    
    
    public void setCarte(Carte carte) { this.carte = carte; }
    public void setIdProprietaire(int idProprietaire) { this.idProprietaire = idProprietaire; }
    public void setTypeRevealation(String typeRevealation) { this.typeRevealation = typeRevealation; }
}
