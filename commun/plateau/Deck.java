package commun.plateau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Deck implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Carte> cartes;
    private int index;

    
    public Deck() {
        this.cartes = new ArrayList<>();
        this.index = 0;
        genererCartes();
    }

    
    private void genererCartes() {
        
        for (int exemplaire = 0; exemplaire < 3; exemplaire++) {
            for (int valeur = 1; valeur <= 12; valeur++) {
                cartes.add(new Carte(valeur, Forme.CERCLE, Couleur.ROUGE, Remplissage.PLEIN));
            }
        }
    }

    
    public void melanger() {
        Collections.shuffle(cartes);
        index = 0;
    }

    
    public List<Carte> tirerCartesInitiales() {
        List<Carte> cartesInitiales = new ArrayList<>();
        for (int i = 0; i < 12 && index < cartes.size(); i++) {
            cartesInitiales.add(cartes.get(index++));
        }
        return cartesInitiales;
    }

    
    public Carte tirerCarte() {
        if (index < cartes.size()) {
            return cartes.get(index++);
        }
        return null;
    }

    
    public void reinitialiser() {
        index = 0;
        melanger();
    }

    
    public int getCartesRestantes() {
        return cartes.size() - index;
    }

    
    public List<Carte> getCartes() {
        return cartes;
    }

    
    public int getTaille() {
        return cartes.size();
    }
}
