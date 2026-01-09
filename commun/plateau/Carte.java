package commun.plateau;
import java.io.Serializable;


public class Carte implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int nextId = 1;  
    
    private int id;                  
    private int valeur;              
    private Forme forme;             
    private Couleur couleur;         
    private Remplissage remplissage; 
    private boolean revelee;         

    
    public Carte(int valeur, Forme forme, Couleur couleur, Remplissage remplissage) {
        this.id = nextId++;
        this.valeur = valeur;
        this.forme = forme;
        this.couleur = couleur;
        this.remplissage = remplissage;
        this.revelee = false;
    }

    
    public int getId() {
        return id;
    }

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

    
    @Override
    public String toString() {
        return valeur + " " + forme.getSymbole() + " " + couleur.getEmoji() + " " + remplissage.getSymbole();
    }

    
    public String getDetails() {
        return valeur + " " + forme.getNom() + " " + couleur.getNom() + " " + remplissage.getNom();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Carte autre = (Carte) obj;
        return valeur == autre.valeur &&
               forme == autre.forme &&
               couleur == autre.couleur &&
               remplissage == autre.remplissage;
    }

    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(valeur, forme, couleur, remplissage);
    }
}
