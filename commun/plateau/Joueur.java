package commun.plateau;

import java.io.Serializable;
import java.util.List;

public class Joueur implements Serializable {
    private static final long serialVersionUID = 1L; 
    private int id;
    private String nom;
    private List<Carte> deck;
    private List<Trio> trios;

    public Joueur(int id, String nom, List<Carte> deck, List<Trio> trios) {
        this.id = id;
        this.nom = nom;
        this.deck = deck;
        this.trios = trios;
    }

    public int getId() {
        return id;
    }
    
    public String getNom() {
    	return nom;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public

    public List<Carte> getDeck() {
        return deck;
    }

    public void setDeck(List<Carte> deck) {
        this.deck = deck;
    }

    public List<Trio> getTrios() {
        return trios;
    }

    public void setTrios(List<Trio> trios) {
        this.trios = trios;
    }
}
