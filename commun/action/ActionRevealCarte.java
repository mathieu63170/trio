package commun.action;

import commun.plateau.Carte;


public class ActionRevealCarte extends Action {
    private static final long serialVersionUID = 1L;
    
    private int idJoueurSource;    
    private Carte carte;           
    private String type;           

    public ActionRevealCarte(int idJoueur, int idJoueurSource, Carte carte, String type) {
        super(idJoueur);
        this.idJoueurSource = idJoueurSource;
        this.carte = carte;
        this.type = type;
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
