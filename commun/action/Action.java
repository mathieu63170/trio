package commun.action;

import java.io.Serializable;

public class Action implements Serializable {
    private static final long serialVersionUID = 1L; 
    private int idJoueur;

    public Action(int idJoueur) {
        this.idJoueur = idJoueur;
    }

    public int getIdJoueur() {
        return idJoueur;
    }

    public void setIdJoueur(int idJoueur) {
        this.idJoueur = idJoueur;
    }
}
