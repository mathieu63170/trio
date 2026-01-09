package commun.action;

import commun.plateau.Carte;
import java.io.Serializable;


public class ActionRevealCarte implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int idJoueur;          
    private int idJoueurSource;    
    private Carte carte;           
    private String type;           

    public ActionRevealCarte(int idJoueur, int idJoueurSource, Carte carte, String type) {
        this.idJoueur = idJoueur;
        this.idJoueurSource = idJoueurSource;
        this.carte = carte;
        this.type = type;
    }

    public int getIdJoueur() {
        return idJoueur;
    }

    public int getIdJoueurSource() {
        return idJoueurSource;
    }

    public Carte getCarte() {
        return carte;
    }

    public String getType() {
        return type;
    }
}
