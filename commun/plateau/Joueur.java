package commun.plateau;

import java.util.List;

public class Joueur {
    private int id;
    private List<Carte> deck;
    private List<Trio> trios;

    public Joueur(int id, List<Carte> deck, List<Trio> trios) {
        this.id = id;
        this.deck = deck;
        this.trios = trios;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
