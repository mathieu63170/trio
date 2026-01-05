package commun.action;

import commun.plateau.Carte;
import java.io.Serializable;

/**
 * ActionRevealCarte - Révèle une carte temporairement
 * Utilisée quand on demande MAX/MIN - la carte est montrée mais pas prise
 */
public class ActionRevealCarte implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int idJoueur;          // Joueur qui reçoit la révélation
    private int idJoueurSource;    // Joueur dont la carte provient
    private Carte carte;           // La carte révélée
    private String type;           // "MAX" ou "MIN"

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
